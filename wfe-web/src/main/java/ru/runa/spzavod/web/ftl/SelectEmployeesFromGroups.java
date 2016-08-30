package ru.runa.spzavod.web.ftl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import freemarker.template.TemplateModelException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.AjaxJsonFormComponent;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * В 4-й версии рекомендуется организовывать списки пользователей, и тогда можно получить доступ ко всем полям. При сохранении в список строк будут
 * сохраняться (уникальные) логины. В сценарии с исключениями работоспособной будет связывание с картой, у которой ключи либо логины пользователей,
 * либо пользователи.
 *
 * При наличии в процессе переменной VARIABLE_groups_included список групп берётся только из неё. При наличии в процессе переменной
 * VARIABLE_groups_excluded указанный список групп исключается из возможного при выборе. Обе переменные могут быть типа Строка, Группа, Список
 * <Строка>, Список<Группа>.
 *
 * @author Dofs
 */
@SuppressWarnings("unchecked")
public class SelectEmployeesFromGroups extends AjaxJsonFormComponent {
    private static final long serialVersionUID = 1L;
    private static final String actorTooltipTemplate = ClassLoaderUtil.getAsString("ru.runa.wfe.user.Actor.tooltip.template", Actor.class);

    @Override
    protected String renderRequest() throws TemplateModelException {
        getExclusions(); // for check purpose
        String variableName = getParameterAs(String.class, 0);
        String displayFormat = getParameterAs(String.class, 1);
        boolean byLogin = "login".equals(displayFormat);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String scriptingVariableName = variable.getDefinition().getScriptingName();
        Map<String, String> substitutions = new HashMap<String, String>();
        substitutions.put("VARIABLENAME", variableName);
        substitutions.put("UNIQUENAME", scriptingVariableName);
        substitutions.put("DIALOG_TITLE", webHelper.getMessage("title.select_employees"));
        substitutions.put("SELECT_ALL_LABEL", webHelper.getMessage("label.select_all_employees"));
        substitutions.put("ACTOR_SELECTED_INFO", webHelper.getMessage("message.actor_selected"));
        StringBuffer groupsOptions = new StringBuffer();
        List<Group> groups = getGroups(variableName + "_groups_included");
        if (groups == null) {
            groups = (List<Group>) Delegates.getExecutorService().getExecutors(user, BatchPresentationFactory.GROUPS.createNonPaged());
        }
        Collections.sort(groups);
        List<Group> excludedGroups = getGroups(variableName + "_groups_excluded");
        if (excludedGroups != null) {
            for (Group excludedGroup : excludedGroups) {
                groups.remove(excludedGroup);
            }
        }
        for (Group group : groups) {
            if (group.getClass() == Group.class) {
                groupsOptions.append("<option>").append(group.getName()).append("</option>");
            }
        }
        substitutions.put("GROUP_OPTIONS", groupsOptions.toString());

        List<String> list = variableProvider.getValue(List.class, variableName);
        if (list == null) {
            list = Lists.newArrayList();
        }

        StringBuilder html = new StringBuilder();
        html.append(exportScript(substitutions, false));
        html.append("<style>div.actorSelected {padding-left: 17px; background: url('/wfe/images/info.png') no-repeat top left;}</style>");
        html.append("<div class='selectEmployees' id='").append(scriptingVariableName).append("'>");
        html.append("<input type='hidden' name='").append(variableName).append(".size' value='").append(list.size()).append("' />");
        for (int row = 0; row < list.size(); row++) {
            html.append("<div row='").append(row).append("' style='margin-bottom:4px;'>");
            Actor actor = TypeConversionUtil.convertToExecutor(list.get(row), new DelegateExecutorLoader(user));
            String actorName = actor != null ? actor.getName() : "";
            html.append("<input type='hidden' name='" + variableName + "[" + row + "]' value='" + actorName + "' /> ");
            html.append("<input value='");
            if (actor != null) {
                html.append(byLogin ? actor.getName() : actor.getFullName());
            } else {
                html.append("");
            }
            html.append("' readonly='true' />");
            html.append(" <input type='button'  onclick='remove").append(scriptingVariableName).append("(this);'");
            String title = getTitle(actor);
            if (!Strings.isNullOrEmpty(title)) {
                html.append(" title='").append(title).append("'");
            }
            html.append(" style='width: 30px;' value=' - '/>");
            html.append("</div>");
        }
        html.append("<div class='selectEmployeesAddButton'>");
        html.append("<input type='button' id='buttonAdd").append(scriptingVariableName).append("' style='width: 30px;' value=' + '/>");
        html.append("</div>");
        html.append("</div>");
        return html.toString();
    }

    private String getTitle(Actor actor) {
        if (!Strings.isNullOrEmpty(actorTooltipTemplate)) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("object", actor);
            IVariableProvider variableProvider = new MapDelegableVariableProvider(map, null);
            return ExpressionEvaluator.process(user, actorTooltipTemplate, variableProvider, webHelper);
        }
        return null;
    }

    private List<Group> getGroups(String variableName) {
        List<?> list = variableProvider.getValue(List.class, variableName);
        if (list != null) {
            List<Group> result = Lists.newArrayList();
            for (Object object : list) {
                Executor executor = TypeConversionUtil.convertToExecutor(object, new DelegateExecutorLoader(user));
                if (executor instanceof Group) {
                    result.add((Group) executor);
                } else {
                    log.error("Variable '" + variableName + "' contains not a group " + executor);
                }
            }
            return result;
        }
        return null;
    }

    @Override
    protected JSONAware processAjaxRequest(HttpServletRequest request) throws Exception {
        JSONArray jsonArray = new JSONArray();
        String displayFormat = getParameterAs(String.class, 1);
        boolean byLogin = "login".equals(displayFormat);
        String groupName = request.getParameter("group");
        String hint = request.getParameter("hint");
        List<Actor> actors = getActors(groupName, byLogin, hint);
        if (actors.size() == 0) {
            jsonArray.add(createJsonObject(null, "", null, null));
        }
        Map<Object, String> exclusions = getExclusions();
        for (Actor actor : actors) {
            String exclusion = exclusions.get(actor);
            if (exclusion == null) {
                exclusion = exclusions.get(actor.getName());
            }
            jsonArray.add(createJsonObject(actor.getName(), byLogin ? actor.getName() : actor.getFullName(), exclusion, getTitle(actor)));
        }
        return jsonArray;
    }

    private JSONObject createJsonObject(Object code, String name, String exclusion, String title) {
        JSONObject object = new JSONObject();
        object.put("code", code != null ? code : "");
        object.put("name", name);
        object.put("exclusion", exclusion != null ? exclusion : "");
        object.put("title", title != null ? title : "");
        return object;
    }

    private Map<Object, String> getExclusions() throws TemplateModelException {
        String exclusionsVariableName = getParameterAs(String.class, 3);
        if (Strings.isNullOrEmpty(exclusionsVariableName)) {
            return Maps.newHashMap();
        }
        String targetProcessIdVariableName = getParameterAs(String.class, 2);
        Long targetProcessId = variableProvider.getValue(long.class, targetProcessIdVariableName);
        Map<Object, String> exclusionsMap;
        if (targetProcessId == 0) {
            exclusionsMap = variableProvider.getValue(Map.class, exclusionsVariableName);
        } else {
            exclusionsMap = (Map<Object, String>) Delegates.getExecutionService().getVariable(user, targetProcessId, exclusionsVariableName)
                    .getValue();
        }
        if (exclusionsMap == null) {
            LogFactory.getLog(getClass()).warn("exclusionsMap = null for " + targetProcessId + ":" + exclusionsVariableName);
            exclusionsMap = Maps.newHashMap();
        }
        return exclusionsMap;
    }

    private List<Actor> getActors(String groupName, boolean byLogin, String hint) throws TemplateModelException {
        hint = hint.toLowerCase();
        if (groupName != null && groupName.length() > 0) {
            List<Actor> list = new ArrayList<Actor>();
            Group group = Delegates.getExecutorService().getExecutorByName(user, groupName);
            List<Actor> groupActors = Delegates.getExecutorService().getGroupActors(user, group);
            for (Actor actor : groupActors) {
                if (byLogin) {
                    if (actor.getName().toLowerCase().startsWith(hint)) {
                        list.add(actor);
                    }
                } else {
                    if (actor.getFullName().toLowerCase().startsWith(hint)) {
                        list.add(actor);
                    }
                }
            }
            Collections.sort(list);
            return list;
        } else {
            BatchPresentation batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
            batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
            if (hint.length() > 0) {
                int filterIndex = byLogin ? 0 : 1;
                batchPresentation.getFilteredFields().put(filterIndex, new StringFilterCriteria(hint + StringFilterCriteria.ANY_SYMBOLS, true));
            }
            return (List<Actor>) Delegates.getExecutorService().getExecutors(user, batchPresentation);
        }
    }
}
