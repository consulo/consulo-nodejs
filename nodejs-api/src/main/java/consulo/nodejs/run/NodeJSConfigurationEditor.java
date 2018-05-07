/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.nodejs.run;

import javax.annotation.Nonnull;
import javax.swing.JComponent;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import consulo.annotations.RequiredDispatchThread;

/**
 * @author VISTALL
 * @since 18.03.14
 */
public class NodeJSConfigurationEditor extends SettingsEditor<NodeJSConfiguration>
{
	private final Project myProject;
	private NodeJSConfigurationPanel myConfigurationPanel;

	public NodeJSConfigurationEditor(Project project)
	{
		myProject = project;
	}

	@Override
	@RequiredDispatchThread
	protected void resetEditorFrom(NodeJSConfiguration nodeJSConfiguration)
	{
		myConfigurationPanel.reset(nodeJSConfiguration);
	}

	@Override
	@RequiredDispatchThread
	protected void applyEditorTo(NodeJSConfiguration nodeJSConfiguration) throws ConfigurationException
	{
		myConfigurationPanel.applyTo(nodeJSConfiguration);
	}

	@Nonnull
	@Override
	protected JComponent createEditor()
	{
		if(myConfigurationPanel == null)
		{
			myConfigurationPanel = new NodeJSConfigurationPanel(myProject);
		}
		return myConfigurationPanel;
	}
}
