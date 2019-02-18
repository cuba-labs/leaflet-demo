package com.haulmont.demo.web.screens.salesperson;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.demo.entity.Salesperson;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;

public class SalespersonBrowse extends AbstractLookup {

    private static final String PHOTO_SIZE = "40";

    @Inject
    private ComponentsFactory componentsFactory;

    @Inject
    private GroupTable<Salesperson> salespersonsTable;

    @Override
    public void init(Map<String, Object> params) {
        salespersonsTable.addGeneratedColumn("name", this::generatePhotoWithNameComponent);
    }

    private Component generatePhotoWithNameComponent(Salesperson entity) {
        Image photo = componentsFactory.createComponent(Image.class);
        photo.setScaleMode(Image.ScaleMode.CONTAIN);
        photo.setHeight(PHOTO_SIZE);
        photo.setWidth(PHOTO_SIZE);

        FileDescriptor photoDescriptor = entity.getPhoto();
        if (Objects.nonNull(photoDescriptor)) {
            photo.setSource(FileDescriptorResource.class).setFileDescriptor(photoDescriptor);
        }

        Label name = componentsFactory.createComponent(Label.class);
        name.setValue(entity.getName());
        name.setAlignment(Alignment.MIDDLE_LEFT);

        HBoxLayout container = componentsFactory.createComponent(HBoxLayout.class);
        container.setSpacing(true);
        container.add(photo);
        container.add(name);

        return container;
    }
}