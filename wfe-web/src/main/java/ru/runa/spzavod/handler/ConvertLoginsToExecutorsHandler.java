package ru.runa.spzavod.handler;

import java.util.List;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;

public class ConvertLoginsToExecutorsHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        List<String> executorLogins = handlerData.getInputParamValueNotNull(List.class, "input");
        List<Executor> result = Lists.newArrayList();
        for (String login : executorLogins) {
            Executor executor = TypeConversionUtil.convertTo(Executor.class, login);
            result.add(executor);
        }
        handlerData.setOutputParam("result", result);
    }

}
