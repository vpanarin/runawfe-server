package ru.runa.wf.web.ftl.component;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ActorFormat;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.VariableFormat;

public class InputVariable extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String html = "<div class=\"inputVariable " + variable.getDefinition().getScriptingNameWithoutDots() + "\">";
        html += getComponentInput(user, webHelper, variable);
        html += "</div>";
        return html;
    }

    private String getComponentInput(User user, WebHelper webHelper, WfVariable variable) {
        final VariableFormat variableFormat = variable.getDefinition().getFormatNotNull();
        if (variableFormat instanceof ListFormat) {
            final VariableFormat componentFormat = FormatCommons.createComponent(variable, 0);
            if (componentFormat instanceof ActorFormat) {
                final SelectEmployeeFromGroupRenderer renderer = new SelectEmployeeFromGroupRenderer(user, variableProvider, webHelper);
                return renderer.createComponent(variable);
            }
        }
        return ViewUtil.getComponentInput(user, webHelper, variable);
    }
}
