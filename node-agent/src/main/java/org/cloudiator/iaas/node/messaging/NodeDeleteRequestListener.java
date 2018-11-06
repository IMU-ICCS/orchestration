package org.cloudiator.iaas.node.messaging;

import com.google.common.base.MoreObjects;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.persistance.NodeDomainRepository;
import javax.inject.Inject;
import org.cloudiator.iaas.node.NodeDeletionStrategy;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeDeleteMessage;
import org.cloudiator.messages.Node.NodeDeleteResponseMessage;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDeleteRequestListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeDeleteRequestListener.class);
  private final MessageInterface messageInterface;
  private final NodeDeletionStrategy nodeDeletionStrategy;
  private final NodeDomainRepository nodeDomainRepository;

  @Inject
  public NodeDeleteRequestListener(MessageInterface messageInterface,
      NodeDeletionStrategy nodeDeletionStrategy,
      NodeDomainRepository nodeDomainRepository) {
    this.messageInterface = messageInterface;
    this.nodeDeletionStrategy = nodeDeletionStrategy;
    this.nodeDomainRepository = nodeDomainRepository;
  }

  @Transactional
  void deleteNode(Node node, String userId) {
    nodeDomainRepository.delete(node.id(), userId);
  }

  @Override
  public void run() {
    messageInterface.subscribe(NodeDeleteMessage.class, NodeDeleteMessage.parser(),
        new MessageCallback<NodeDeleteMessage>() {
          @Override
          public void accept(String id, NodeDeleteMessage content) {

            try {

              final String userId = content.getUserId();
              final String nodeId = content.getNodeId();

              LOGGER.debug(String
                  .format("%s is receiving request to delete node %s from user %s.", this, nodeId,
                      userId));

              Node node = nodeDomainRepository.findByTenantAndId(userId, nodeId);

              if (node == null) {
                LOGGER
                    .warn(String
                        .format("%s can not delete node with id %s as it does not exist.", this,
                            nodeId));
                messageInterface.reply(NodeDeleteResponseMessage.class, id,
                    Error.newBuilder().setCode(404)
                        .setMessage(String.format("Node with id %s does not exist.", nodeId))
                        .build());
                return;
              }

              LOGGER.info(String.format("%s is requests deletion of node %s.", this, node));

              boolean b = nodeDeletionStrategy.deleteNode(node, userId);

              if (!b) {
                LOGGER.error(String.format("%s was not able to delete node %s", this, node));
                messageInterface.reply(NodeDeleteResponseMessage.class, id,
                    Error.newBuilder().setCode(500).setMessage("Unable to delete node " + node)
                        .build());
                return;
              }

              LOGGER.debug(String.format("%s is deleting node %s from the database.", this, node));
              deleteNode(node, userId);

              LOGGER.info(String.format("%s has successfully deleted node %s.", this, node));
              messageInterface.reply(id, NodeDeleteResponseMessage.newBuilder().build());

            } catch (Exception e) {
              LOGGER.error("Unexpected error while deleting node:" + e.getMessage(), e);
              messageInterface.reply(NodeDeleteResponseMessage.class, id,
                  Error.newBuilder().setCode(500)
                      .setMessage("Unexpected error while deleting node:" + e.getMessage())
                      .build());
            }


          }
        });
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}