/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 *  All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 *
 * @author bfitzpat
 ******************************************************************************/
package org.switchyard.tools.ui.editor.components.jca;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.soa.sca.sca1_1.model.sca.Binding;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.switchyard.tools.models.switchyard1_0.jca.JCABinding;
import org.switchyard.tools.models.switchyard1_0.jca.JcaPackage;
import org.switchyard.tools.models.switchyard1_0.jca.Property;
import org.switchyard.tools.ui.editor.Messages;
import org.switchyard.tools.ui.editor.databinding.EMFUpdateValueStrategyNullForEmptyString;
import org.switchyard.tools.ui.editor.databinding.SWTValueUpdater;

/**
 * @author bfitzpat
 *
 */
public class JCACCIEndpointPropertiesExtension implements
        IJCAEndpointPropertiesExtension {

    /*
     * These properties correspond to setters on the CCIEndpont class, e.g.
     * setConnectionFactoryJNDIName().
     */
    private static final String CONNECTION_FACTORY_JNDI_NAME_PROP = "connectionFactoryJNDIName"; //$NON-NLS-1$
    @SuppressWarnings("serial")
    private static final Set<String> HIDDEN_PROPERTIES = new HashSet<String>() {
        {
            add(CONNECTION_FACTORY_JNDI_NAME_PROP);
        }
    };

    @Override
    public AbstractJCABindingComposite createComposite(FormToolkit toolkit) {
        return new JCACCIEndpointPropertiesComposite(toolkit);
    }

    private static final class JCACCIEndpointPropertiesComposite extends AbstractJCABindingComposite {

        /**
         * @param toolkit Form toolkit to use when creating controls
         */
        private JCACCIEndpointPropertiesComposite(FormToolkit toolkit) {
            super(toolkit);
        }

        private JCABinding _binding;
        private Composite _panel;
        private Text _connectionFactoryJNDINameText;
        private JCAInteractionPropertyTable _propsList;
        private WritableValue _bindingValue;

        @Override
        public String getTitle() {
            return Messages.JCACCIEndpointPropertiesExtension_Title;
        }

        @Override
        public String getDescription() {
            return getTitle();
        }

        @Override
        protected boolean validate() {
            return true;
        }

        @Override
        public void createContents(Composite parent, int style, DataBindingContext context) {
            _panel = getToolkit().createComposite(parent, style);
            _panel.setLayout(new GridLayout(2, false));

            _connectionFactoryJNDINameText = createLabelAndText(_panel, Messages.JCAJMSEndpointPropertiesExtension_ConnectionFactoryJNDIName_label);

            Group endpointPropsGroup = new Group(_panel, SWT.NONE);
            endpointPropsGroup.setText(getTitle());
            endpointPropsGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 3, 1));
            endpointPropsGroup.setLayout(new GridLayout(1, false));
            getToolkit().adapt(endpointPropsGroup, false, false);

            _propsList = new JCAInteractionPropertyTable(endpointPropsGroup, SWT.NONE, getToolkit(), context, getDomain(getTargetObject()));
            _propsList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
            _propsList.getTableViewer().addFilter(new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    final String name = ((Property) element).getName();
                    return !(name != null && HIDDEN_PROPERTIES.contains(name));
                }
            });
            getToolkit().adapt(_propsList, false, false);
            
            bindControls(context);
        }

        private void bindControls(DataBindingContext context) {
            final EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(getTargetObject());
            final Realm realm = SWTObservables.getRealm(_panel.getDisplay());

            _bindingValue = new WritableValue(realm, null, JCABinding.class);

            final FeaturePath propertiesFeaturePath = FeaturePath.fromList(
                    JcaPackage.Literals.JCA_BINDING__INBOUND_INTERACTION,
                    JcaPackage.Literals.JCA_INBOUND_INTERACTION__ENDPOINT, JcaPackage.Literals.ENDPOINT__PROPERTY);
            final IObservableList propertiesList = (domain == null ? EMFProperties.list(propertiesFeaturePath)
                    : EMFEditProperties.list(domain, propertiesFeaturePath)).observeDetail(_bindingValue);

            org.eclipse.core.databinding.Binding binding = context.bindValue(
                    SWTObservables.observeText(_connectionFactoryJNDINameText, SWT.Modify),
                    new JCANamedPropertyObservableValue(realm, propertiesList, CONNECTION_FACTORY_JNDI_NAME_PROP),
                    new EMFUpdateValueStrategyNullForEmptyString(null, UpdateValueStrategy.POLICY_CONVERT), null);
            ControlDecorationSupport.create(SWTValueUpdater.attach(binding), SWT.TOP | SWT.LEFT, _panel);
        }

        @Override
        public Composite getPanel() {
            return _panel;
        }

        @Override
        public void setBinding(Binding impl) {
            super.setBinding(impl);
            _binding = (JCABinding) impl;
            _bindingValue.setValue(_binding);
            _propsList.setTargetObject(_binding);
        }

    }
}
