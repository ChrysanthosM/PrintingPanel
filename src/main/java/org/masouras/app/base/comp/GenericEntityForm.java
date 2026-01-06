package org.masouras.app.base.comp;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.BeanValidator;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EmbeddedId;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.masouras.model.mssql.schema.jpa.control.vaadin.FieldFactory;
import org.masouras.model.mssql.schema.jpa.control.vaadin.FormField;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class GenericEntityForm<T> extends FormLayout {
    private final Class<T> entityClass;
    private final Binder<T> binder;

    private final Span validationStatus = new Span();

    private final Map<Field, Component> fieldComponents = new HashMap<>();
    private final List<Field> keyFields = new ArrayList<>();

    private T entity;
    @Setter private Runnable onSaveCallback;


    public GenericEntityForm(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.binder = new Binder<>(this.entityClass);
    }
    @PostConstruct
    private void init() {
        addComponents();
        addStyle();
    }
    private void addComponents() {
        binder.setStatusLabel(validationStatus);

        buildForm(entityClass);
        add(buildButtonBar());
        add(validationStatus);
    }
    private void addStyle() {
        validationStatus.getStyle().set("color", "var(--lumo-error-text-color)");
        setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("600px", 2)
        );
    }

    private Component buildButtonBar() {
        return new HorizontalLayout(
                new Button(new Icon(VaadinIcon.DISC), e -> saveEntity()),
                new Button("Cancel", e -> setEntity(null))
        );
    }

    private void buildForm(Class<T> entityClass) {
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(EmbeddedId.class))
                .forEach(field -> {
                    addGroupHeader("Key Fields");
                    buildEmbeddedIdFields(field);
                });

        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(FormField.class))
                .filter(field -> !field.isAnnotationPresent(EmbeddedId.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(FormField.class).order()))
                .collect(Collectors.groupingBy(field -> StringUtils.defaultIfBlank(field.getAnnotation(FormField.class).group(), "Attribute Fields")))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    addGroupHeader(entry.getKey());
                    entry.getValue().forEach(this::addFieldComponent);
                });
    }
    private void buildEmbeddedIdFields(Field embeddedField) {
        Arrays.stream(embeddedField.getType().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(FormField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(FormField.class).order()))
                .forEach(keyField -> {
                    FormField formField = keyField.getAnnotation(FormField.class);
                    Component component = FieldFactory.createField(keyField, formField);

                    fieldComponents.put(keyField, component);
                    if (formField.key()) keyFields.add(keyField);

                    add(component);
                    setColspan(component, formField.colspan());

                    buildEmbeddedIdField(embeddedField, keyField, component, formField);
                });
    }
    private void addGroupHeader(String groupName) {
        if (StringUtils.isBlank(groupName)) return;
        H3 header = new H3(groupName);
        add(header);
        setColspan(header, 2);
    }
    private void addFieldComponent(Field field) {
        FormField formField = field.getAnnotation(FormField.class);
        Component component = FieldFactory.createField(field, formField);

        fieldComponents.put(field, component);
        if (formField.key()) keyFields.add(field);

        add(component);
        setColspan(component, formField.colspan());

        bindField(field, component, formField);
    }
    @SuppressWarnings("unchecked")
    private void buildEmbeddedIdField(Field embeddedField, Field keyField, Component component, FormField formField) {
        String propertyPath = embeddedField.getName() + "." + keyField.getName();
        if (component instanceof HasValue<?, ?> hv) {
            Binder.BindingBuilder<T, Object> builder = getBindingBuilder(binder.forField((HasValue<?, Object>) hv), formField, propertyPath);
            builder.bind(propertyPath);
        }
    }
    @SuppressWarnings("unchecked")
    private void bindField(Field field, Component component, FormField formField) {
        field.setAccessible(true);
        if (component instanceof HasValue<?, ?> hv) {
            Binder.BindingBuilder<T, Object> builder = getBindingBuilder(binder.forField((HasValue<?, Object>) hv), formField, field.getName());
            builder.bind(e -> getFieldValue(field, e), (e, v) -> setFieldValue(field, e, v));
        }
    }
    private Binder.BindingBuilder<T, Object> getBindingBuilder(Binder.BindingBuilder<T, Object> binder, FormField formField, String field) {
        Binder.BindingBuilder<T, Object> builder = binder;
        if (formField.required()) builder = builder.asRequired(formField.label() + " is required");
        // 2. Hibernate Validator support
        builder = builder.withValidator(new BeanValidator(entityClass, field));
        return builder;
    }

    private Object getFieldValue(Field field, T entity) {
        try {
            return field.get(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setFieldValue(Field field, T entity, Object value) {
        try {
            field.set(entity, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void setEntity(T entity) {
        clearFields();
        this.entity = entity;
        if (entity == null) {
            setVisible(false);
            return;
        }

        initializeEmbeddedId(entity);
        binder.readBean(entity);

        updateKeyFieldState(entity);
        setVisible(true);
    }
    private void initializeEmbeddedId(T entity) {
        Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(EmbeddedId.class))
                .forEach(field -> {
                    field.setAccessible(true);
                    initializeEmbeddedIdMain(entity, field);
                });
    }
    private void initializeEmbeddedIdMain(T entity, Field field) {
        try {
            if (field.get(entity) == null) {
                Object keyInstance = field.getType().getDeclaredConstructor().newInstance();
                field.set(entity, keyInstance);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize embedded ID", e);
        }
    }
    private void updateKeyFieldState(T entity) {
        boolean editing = isEditing(entity);
        keyFields.forEach(field -> {
            Component component = fieldComponents.get(field);
            if (component != null) {
                component.getElement().setEnabled(!editing);
            }
        });
    }
    private boolean isEditing(T entity) {
        try {
            Field idField = Arrays.stream(entity.getClass().getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(EmbeddedId.class))
                    .findFirst()
                    .orElse(null);
            if (idField == null) return false;

            idField.setAccessible(true);
            Object id = idField.get(entity);
            if (id == null) return false;

            return Arrays.stream(id.getClass().getDeclaredFields()).allMatch(field -> checkFieldNotNull(field, id));
        } catch (Exception e) {
            return false;
        }
    }
    private boolean checkFieldNotNull(Field field, Object id) {
        try {
            field.setAccessible(true);
            return field.get(id) != null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveEntity() {
        if (!binder.validate().isOk()) {
            Notification.show("Please fix the errors", 3000, Notification.Position.MIDDLE);
            return;
        }
        if (binder.writeBeanIfValid(entity)) {
            onSave(entity);
            setEntity(null);
        }
    }

    protected abstract void onSave(T entity);
    protected void onSaveMain(@Nullable String NotificationMessage) {
        if (this.onSaveCallback != null) this.onSaveCallback.run();

        if (StringUtils.isNotBlank(NotificationMessage)) {
            Notification.show(NotificationMessage, 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }

    private void clearFields() {
        fieldComponents.values().stream()
                .filter(component -> component instanceof HasValue<?, ?>)
                .forEach(component -> ((HasValue<?, ?>) component).clear());
    }
}
