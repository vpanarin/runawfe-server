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
package ru.runa.common.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.ForwardAction;

import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.servlet.UploadedFile;

import java.util.Iterator;

/**
 */
public class TabHeaderForwardAction extends ForwardAction {

    private final static String TAB_FORWARD_NAME_PARAMETER_NAME = "tabForwardName";

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String tabForwardName = request.getParameter(TAB_FORWARD_NAME_PARAMETER_NAME);
        if (tabForwardName != null) {
            TabHttpSessionHelper.setTabForwardName(tabForwardName, request.getSession());

            //bug fix #1095, not right place but most acceptable solution
            Iterator<UploadedFile> it = FormSubmissionUtils.getUploadedFilesMap(request).values().iterator();
            while (it.hasNext()) {
                UploadedFile file = it.next();
                if (file.isFlagFor1095()) {
                    it.remove();
                }
            }
        }

        return super.execute(mapping, form, request, response);
    }
}
