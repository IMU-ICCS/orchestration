/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

class NodeConverter implements OneWayConverter<NodeModel, Node> {

  private final NodePropertiesConverter nodePropertiesConverter = new NodePropertiesConverter();
  private final LoginCredentialConverter loginCredentialConverter = new LoginCredentialConverter();
  private final IpAddressConverter ipAddressConverter = new IpAddressConverter();

  @Nullable
  @Override
  public Node apply(@Nullable NodeModel nodeModel) {
    if (nodeModel == null) {
      return null;
    }

    NodeBuilder nodeBuilder = NodeBuilder.newBuilder()
        .id(nodeModel.getDomainId())
        .originId(nodeModel.getOriginId())
        .name(nodeModel.getName())
        .loginCredential(loginCredentialConverter.apply(nodeModel.getLoginCredential()))
        .nodeProperties(nodePropertiesConverter.apply(nodeModel.getNodeProperties()))
        .nodeType(nodeModel.getType())
        .userId(nodeModel.getTenantModel().getUserId())
        .state(nodeModel.getNodeState())
        .nodeCandidate(nodeModel.getNodeCandidate())
        .reason(nodeModel.getReason())
        .diagnostic(nodeModel.getDiagnostic());

    nodeBuilder.ipAddresses(
        nodeModel.ipAddresses().stream().map(ipAddressConverter).collect(Collectors.toSet()));

    return nodeBuilder.build();

  }
}
