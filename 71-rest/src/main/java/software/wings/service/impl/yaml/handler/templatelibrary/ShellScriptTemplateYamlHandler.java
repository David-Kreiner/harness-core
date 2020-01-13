package software.wings.service.impl.yaml.handler.templatelibrary;

import com.google.inject.Singleton;

import software.wings.beans.template.BaseTemplate;
import software.wings.beans.template.Template;
import software.wings.beans.template.command.ShellScriptTemplate;
import software.wings.yaml.templatelibrary.ShellScriptTemplateYaml;

@Singleton
public class ShellScriptTemplateYamlHandler extends TemplateLibraryYamlHandler<ShellScriptTemplateYaml> {
  @Override
  public ShellScriptTemplateYaml toYaml(Template bean, String appId) {
    ShellScriptTemplate shellScriptTemplateBean = (ShellScriptTemplate) bean.getTemplateObject();
    ShellScriptTemplateYaml shellScriptTemplateYaml = ShellScriptTemplateYaml.builder()
                                                          .outputVars(shellScriptTemplateBean.getOutputVars())
                                                          .scriptString(shellScriptTemplateBean.getScriptString())
                                                          .scriptType(shellScriptTemplateBean.getScriptType())
                                                          .timeOutMillis(shellScriptTemplateBean.getTimeoutMillis())
                                                          .build();
    super.toYaml(shellScriptTemplateYaml, bean);
    return shellScriptTemplateYaml;
  }

  @Override
  protected void setBaseTemplate(Template template, ShellScriptTemplateYaml yaml) {
    BaseTemplate baseTemplate = ShellScriptTemplate.builder()
                                    .scriptString(yaml.getScriptString())
                                    .outputVars(yaml.getOutputVars())
                                    .scriptType(yaml.getScriptType())
                                    .timeoutMillis(yaml.getTimeoutMillis())
                                    .build();
    template.setTemplateObject(baseTemplate);
  }
}
