/*
 * Copyright 2010, Juergen Kellerer and other contributors.
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

package net.sf.logsupport.ui.util;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Binds JComponents to a data bean.
 *
 * @author Juergen_Kellerer, 2010-04-08
 * @version 1.0
 */
public class JComponentBinder<E> {

	private static String createAccessorName(String prefix, String fieldName) {
		return prefix + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	}

	private static Method findMethod(Class<?> cls, String[] names, Class<?>... signature) {
		for (String name : names) {
			try {
				return cls.getMethod(name, signature);
			} catch (NoSuchMethodException e) {
				// ignore
			}
		}
		return null;
	}

	@NotNull
	Binder[] binders;
	private E dataBean;
	
	public JComponentBinder(Object componentBean, E dataBean, String... names) {
		this.dataBean = dataBean;
		List<String> includedNames = Arrays.asList(names);
		List<Binder> binders = new ArrayList<Binder>();

		for (Field field : componentBean.getClass().getDeclaredFields()) {
			if (!JComponent.class.isAssignableFrom(field.getType()))
				continue;
			if (!includedNames.isEmpty() && !includedNames.contains(field.getName()))
				continue;

			JComponent component = null;
			try {
				field.setAccessible(true);
				component = (JComponent) field.get(componentBean);
				if (component == null)
					continue;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			Method getter = findMethod(dataBean.getClass(), new String[]{
					createAccessorName("get", field.getName()),
					createAccessorName("is", field.getName())});
			Method setter = getter == null ? null : findMethod(dataBean.getClass(), new String[]{
					createAccessorName("set", field.getName())}, getter.getReturnType());

			if (setter != null)
				binders.add(createBinder(component, getter, setter));
		}

		this.binders = binders.toArray(new Binder[binders.size()]);
	}

	public void apply(E dataBean) throws BindFailedException {
		List<BindFailed> failures = new ArrayList<BindFailed>();

		for (Binder binder : binders)
			if (binder.getComponent().isEnabled())
				try {
					binder.apply(dataBean);
				} catch (RuntimeException e) {
					failures.add(new BindFailed(binder.getComponent(), e));
				}

		if (!failures.isEmpty())
			throw new BindFailedException(failures);
	}

	public void reset(E dataBean) throws BindFailedException {
		List<BindFailed> failures = new ArrayList<BindFailed>();

		for (Binder binder : binders)
			try {
				binder.reset(dataBean);
			} catch (RuntimeException e) {
				failures.add(new BindFailed(binder.getComponent(), e));
			}

		if (!failures.isEmpty())
			throw new BindFailedException(failures);
	}

	public void setEnabled(boolean enabled) {
		for (Binder binder : binders)
			binder.getComponent().setEnabled(enabled);
	}

	@NotNull
	public List<JComponent> getBoundComponents() {
		List<JComponent> components = new ArrayList<JComponent>(binders.length);
		for (Binder binder : binders)
			components.add(binder.getComponent());
		return components;
	}

	private static abstract class Binder {

		JComponent component;

		protected Binder(JComponent component) {
			this.component = component;
		}

		abstract void apply(Object dataBean);

		abstract void reset(Object dataBean);

		JComponent getComponent() {
			return component;
		}
	}

	private static Binder createBinder(JComponent component, final Method getter, final Method setter) {
		if (component instanceof JTextComponent) {
			return new Binder(component) {
				public void apply(Object dataBean) {
					String txt = ((JTextComponent) component).getText();
					if (txt != null) {
						txt = txt.trim();
						if (txt.isEmpty())
							txt = null;
					}

					try {
						setter.invoke(dataBean, txt);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

				public void reset(Object dataBean) {
					try {
						String txt = (String) getter.invoke(dataBean);
						((JTextComponent) component).setText(txt == null ? "" : txt);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			};
		} else if (component instanceof JToggleButton) {
			return new Binder(component) {
				public void apply(Object dataBean) {
					try {
						setter.invoke(dataBean, ((JToggleButton) component).isSelected());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

				public void reset(Object dataBean) {
					try {
						((JToggleButton) component).setSelected((Boolean) getter.invoke(dataBean));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			};
		} else if (component instanceof JComboBox) {
			return new Binder(component) {
				public void apply(Object dataBean) {
					try {
						setter.invoke(dataBean, ((JComboBox) component).getSelectedItem());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

				public void reset(Object dataBean) {
					try {
						((JComboBox) component).setSelectedItem(getter.invoke(dataBean));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			};
		} else if (component instanceof JSpinner) {
			return new Binder(component) {
				@Override
				void apply(Object dataBean) {
					try {
						setter.invoke(dataBean, ((JSpinner) component).getValue());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				void reset(Object dataBean) {
					try {
						((JSpinner) component).setValue(getter.invoke(dataBean));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			};
		}

		return null;
	}	
}
