package io.github.cloudiator.virtualmachine;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class VmAgentModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(VirtualMachineRequestQueue.class).in(Singleton.class);
  }
}
