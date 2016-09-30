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
package ru.runa.wfe.service.impl;

import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.logic.AuthorizationLogic;
import ru.runa.wfe.service.decl.AuthorizationServiceLocal;
import ru.runa.wfe.service.decl.AuthorizationServiceRemote;
import ru.runa.wfe.service.decl.AuthorizationServiceRemoteWS;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;

/**
 * Implements AuthorizationService as bean. Created on 20.07.2004
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "AuthorizationAPI", serviceName = "AuthorizationWebService")
@SOAPBinding
public class AuthorizationServiceBean implements AuthorizationServiceLocal, AuthorizationServiceRemote, AuthorizationServiceRemoteWS {
    @Autowired
    private AuthorizationLogic authorizationLogic;

    @Override
    @WebResult(name = "result")
    public boolean isAllowed(@WebParam(name = "user") User user, @WebParam(name = "permission") Permission permission,
            @WebParam(name = "identifiable") Identifiable identifiable) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(permission != null, "permission");
        Preconditions.checkArgument(identifiable != null, "identifiable");
        return authorizationLogic.isPermissionAllowed(user, identifiable, permission);
    }

    @WebMethod(exclude = true)
    @Override
    public boolean isAllowed(@WebParam(name = "user") User user, @WebParam(name = "permission") Permission permission,
            @WebParam(name = "securedObjectType") SecuredObjectType securedObjectType, @WebParam(name = "identifiableId") Long identifiableId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(permission != null, "permission");
        Preconditions.checkArgument(securedObjectType != null, "securedObjectType");
        Preconditions.checkArgument(identifiableId != null, "identifiableId");
        return authorizationLogic.isAllowed(user, permission, securedObjectType, identifiableId);
    }

    @WebMethod(exclude = true)
    @Override
    public <T extends Identifiable> boolean[] isAllowed(User user, Permission permission, List<T> identifiables) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(permission != null, "permission");
        Preconditions.checkArgument(identifiables != null, "identifiables");
        return authorizationLogic.isAllowed(user, permission, identifiables);
    }

    @Override
    @WebResult(name = "result")
    public List<Permission> getIssuedPermissions(@WebParam(name = "user") User user, @WebParam(name = "performer") Executor performer,
            @WebParam(name = "identifiable") Identifiable identifiable) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(performer != null, "performer");
        Preconditions.checkArgument(identifiable != null, "identifiable");
        return authorizationLogic.getIssuedPermissions(user, performer, identifiable);
    }

    @WebMethod(exclude = true)
    @Override
    public void setPermissions(User user, List<Long> executorIds, List<Collection<Permission>> permissions, Identifiable identifiable) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executorIds != null, "executorIds");
        Preconditions.checkArgument(permissions != null, "permissions");
        Preconditions.checkArgument(identifiable != null, "identifiable");
        authorizationLogic.setPermissions(user, executorIds, permissions, identifiable);
    }

    @Override
    @WebResult(name = "result")
    public void setPermissions(@WebParam(name = "user") User user, @WebParam(name = "executorId") Long executorId,
            @WebParam(name = "permissions") Collection<Permission> permissions, @WebParam(name = "identifiable") Identifiable identifiable) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executorId != null, "executorId");
        Preconditions.checkArgument(permissions != null, "permissions");
        Preconditions.checkArgument(identifiable != null, "identifiable");
        authorizationLogic.setPermissions(user, executorId, permissions, identifiable);
    }

    @WebMethod(exclude = true)
    @Override
    public void setPermissions(User user, List<Long> executorsId, Collection<Permission> permissions, Identifiable identifiable) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executorsId != null, "executorsId");
        Preconditions.checkArgument(permissions != null, "permissions");
        Preconditions.checkArgument(identifiable != null, "identifiable");
        authorizationLogic.setPermissions(user, executorsId, permissions, identifiable);
    }

    @Override
    @WebResult(name = "result")
    public List<Executor> getExecutorsWithPermission(@WebParam(name = "user") User user, @WebParam(name = "identifiable") Identifiable identifiable,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "withPermission") boolean withPermission) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(identifiable != null, "identifiable");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        return (List<Executor>) authorizationLogic.getExecutorsWithPermission(user, identifiable, batchPresentation, withPermission);
    }

    @Override
    @WebResult(name = "result")
    public int getExecutorsWithPermissionCount(@WebParam(name = "user") User user, @WebParam(name = "identifiable") Identifiable identifiable,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "withPermission") boolean withPermission) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(identifiable != null, "identifiable");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        return authorizationLogic.getExecutorsWithPermissionCount(user, identifiable, batchPresentation, withPermission);
    }

    @Override
    @SuppressWarnings("unchecked")
    @WebResult(name = "result")
    public <T extends Object> List<T> getPersistentObjects(@WebParam(name = "user") User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "persistentClass") Class<T> persistentClass,
            @WebParam(name = "permission") Permission permission, @WebParam(name = "securedObjectTypes") SecuredObjectType[] securedObjectTypes,
            @WebParam(name = "enablePaging") boolean enablePaging) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(batchPresentation != null, "batchPresentation");
        Preconditions.checkArgument(persistentClass != null, "persistenceClass");
        Preconditions.checkArgument(permission != null, "permission");
        Preconditions.checkArgument(securedObjectTypes != null, "securedObjectTypes");
        return (List<T>) authorizationLogic.getPersistentObjects(user, batchPresentation, permission, securedObjectTypes, enablePaging);
    }

    @Override
    @WebResult(name = "result")
    public boolean isAllowedWS(@WebParam(name = "user") User user, @WebParam(name = "permission") Permission permission,
            @WebParam(name = "securedObjectType") SecuredObjectType securedObjectType, @WebParam(name = "identifiableId") Long identifiableId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(permission != null, "permission");
        Preconditions.checkArgument(securedObjectType != null, "securedObjectType");
        Preconditions.checkArgument(identifiableId != null, "identifiableId");
        return authorizationLogic.isAllowed(user, permission, securedObjectType, identifiableId);
    }

}
