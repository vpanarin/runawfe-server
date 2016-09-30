package ru.runa.wfe.execution.async;

import ru.runa.wfe.commons.Utils;

/**
 *
 * @author Alex Chernyshev
 */
public class JMSNodeAsyncExecutor implements INodeAsyncExecutor {

    @Override
    public void execute(Long processId, Long tokenId, String nodeId) {
        Utils.sendNodeAsyncExecutionMessage(processId, tokenId, nodeId);
    }

}