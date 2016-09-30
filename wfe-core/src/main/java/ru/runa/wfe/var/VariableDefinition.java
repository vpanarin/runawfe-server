/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.var;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
public class VariableDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean synthetic;
    private String name;
    private String scriptingName;
    private String description;
    private String format;
    private String formatLabel;
    private UserType userType;
    // web-service serialization limitation
    private UserType[] formatComponentUserTypes;
    private boolean publicAccess;
    private Object defaultValue;
    private transient VariableFormat variableFormat;

    public VariableDefinition() {
    }

    public VariableDefinition(String name, String scriptingName) {
        this.name = name;
        if (scriptingName == null) {
            this.scriptingName = toScriptingName(name);
            this.synthetic = true;
        } else {
            this.scriptingName = scriptingName;
        }
    }

    public VariableDefinition(String name, String scriptingName, VariableFormat variableFormat) {
        this(name, scriptingName);
        setFormat(variableFormat.toString());
        this.variableFormat = variableFormat;
        if (variableFormat instanceof UserTypeFormat) {
            this.userType = ((UserTypeFormat) variableFormat).getUserType();
        }
        if (variableFormat instanceof VariableFormatContainer) {
            String[] componentFormats = getFormatComponentClassNames();
            this.formatComponentUserTypes = new UserType[componentFormats.length];
            for (int i = 0; i < componentFormats.length; i++) {
                this.formatComponentUserTypes[i] = ((VariableFormatContainer) variableFormat).getComponentUserType(i);
            }
        }
    }

    public VariableDefinition(String name, String scriptingName, String format, UserType userType) {
        this(name, scriptingName);
        setFormat(format);
        this.userType = userType;
    }

    public VariableDefinition(String name, String scriptingName, VariableDefinition attributeDefinition) {
        this(name, scriptingName, attributeDefinition.getFormat(), attributeDefinition.getUserType());
        this.formatComponentUserTypes = attributeDefinition.getFormatComponentUserTypes();
        setDefaultValue(attributeDefinition.getDefaultValue());
    }

    public void initComponentUserTypes(ProcessDefinition processDefinition) {
        String[] componentFormats = getFormatComponentClassNames();
        this.formatComponentUserTypes = new UserType[componentFormats.length];
        for (int i = 0; i < componentFormats.length; i++) {
            this.formatComponentUserTypes[i] = processDefinition.getUserType(componentFormats[i]);
        }
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScriptingName() {
        return scriptingName;
    }

    public String getScriptingNameWithoutDots() {
        return scriptingName.replaceAll("\\.", "_").replaceAll("\\[", "_").replaceAll("\\]", "_");
    }

    public VariableFormat getFormatNotNull() {
        if (variableFormat == null) {
            variableFormat = FormatCommons.create(this);
        }
        return variableFormat;
    }

    public String getFormatClassName() {
        if (format != null && format.contains(VariableFormatContainer.COMPONENT_PARAMETERS_START)) {
            int index = format.indexOf(VariableFormatContainer.COMPONENT_PARAMETERS_START);
            return format.substring(0, index);
        }
        return format;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String[] getFormatComponentClassNames() {
        if (format != null && format.contains(VariableFormatContainer.COMPONENT_PARAMETERS_START)) {
            int index = format.indexOf(VariableFormatContainer.COMPONENT_PARAMETERS_START);
            String raw = format.substring(index + 1, format.length() - 1);
            return raw.split(VariableFormatContainer.COMPONENT_PARAMETERS_DELIM, -1);
        }
        return new String[0];
    }

    public boolean isPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFormatLabel() {
        if (formatLabel != null) {
            return formatLabel;
        }
        if (getUserType() != null) {
            return getUserType().getName();
        }
        return format;
    }

    public void setFormatLabel(String formatLabel) {
        this.formatLabel = formatLabel;
    }

    public boolean isUserType() {
        return getUserType() != null;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public UserType[] getFormatComponentUserTypes() {
        return formatComponentUserTypes;
    }

    public List<VariableDefinition> expandUserType(boolean preserveComplex) {
        return expandUserType(this, this, preserveComplex);
    }

    private List<VariableDefinition> expandUserType(VariableDefinition superVariableDefinition, VariableDefinition variableDefinition,
            boolean preserveUserTypeVariables) {
        List<VariableDefinition> result = Lists.newArrayList();
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            String name = superVariableDefinition.getName() + UserType.DELIM + attributeDefinition.getName();
            String scriptingName = superVariableDefinition.getScriptingName() + UserType.DELIM + attributeDefinition.getScriptingName();
            VariableDefinition variable = new VariableDefinition(name, scriptingName, attributeDefinition);
            if (variable.isUserType()) {
                if (preserveUserTypeVariables) {
                    result.add(variable);
                }
                result.addAll(expandUserType(variable, attributeDefinition, preserveUserTypeVariables));
            } else {
                result.add(variable);
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VariableDefinition) {
            VariableDefinition d = (VariableDefinition) obj;
            return Objects.equal(name, d.name);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", getName()).add("format", format).toString();
    }

    public static String toScriptingName(String variableName) {
        char[] chars = variableName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(chars[i])) {
                    chars[i] = '_';
                }
            } else {
                if (!Character.isJavaIdentifierPart(chars[i])) {
                    chars[i] = '_';
                }
            }
            if ('$' == chars[i]) {
                chars[i] = '_';
            }
        }
        String scriptingName = new String(chars);
        return scriptingName;
    }

}
