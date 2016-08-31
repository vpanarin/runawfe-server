package ru.runa.wf.web.ftl.component;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.commons.web.WebUtils;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

public class SelectEmployeeFromGroupRenderer {
    private static final String actorTooltipTemplate = ClassLoaderUtil.getAsString("ru.runa.wfe.user.Actor.tooltip.template", Actor.class);

    private final Log log = LogFactory.getLog(getClass());
    private final User user;
    private final IVariableProvider variableProvider;
    private final WebHelper webHelper;

    public SelectEmployeeFromGroupRenderer(User user, IVariableProvider variableProvider, WebHelper webHelper) {
        super();
        this.user = user;
        this.variableProvider = variableProvider;
        this.webHelper = webHelper;
    }

    public String createComponent(final WfVariable variable) {
        final String variableName = variable.getDefinition().getName();
        final String scriptingVariableName = variable.getDefinition().getScriptingName();
        Map<String, String> substitutions = new HashMap<String, String>();
        substitutions.put("VARIABLENAME", variableName);
        substitutions.put("UNIQUENAME", scriptingVariableName);
        substitutions.put("DIALOG_TITLE", webHelper.getMessage("title.select_employees"));
        substitutions.put("SELECT_ALL_LABEL", webHelper.getMessage("label.select_all_employees"));
        substitutions.put("ACTOR_SELECTED_INFO", webHelper.getMessage("message.actor_selected"));
        substitutions.put("JSON_URL", "/wfe/ajaxcmd?command=ajaxActorsListFromGroup");
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
        InputStream javascriptStream = ClassLoaderUtil.getAsStreamNotNull("scripts/SelectEmployeesFromGroups.js",
                SelectEmployeeFromGroupRenderer.class);
        html.append(WebUtils.getFormComponentScript(javascriptStream, substitutions));
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
                html.append(actor.getFullName());
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

    private String getTitle(Actor actor) {
        if (!Strings.isNullOrEmpty(actorTooltipTemplate)) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("object", actor);
            IVariableProvider variableProvider = new MapDelegableVariableProvider(map, null);
            return ExpressionEvaluator.process(user, actorTooltipTemplate, variableProvider, webHelper);
        }
        return null;
    }
}
