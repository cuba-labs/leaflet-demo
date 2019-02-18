package com.haulmont.demo.web.screens.salesperson;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.cuba.web.gui.components.WebFileDescriptorResource;
import com.haulmont.demo.entity.Salesperson;
import com.vaadin.server.Resource;
import com.vaadin.ui.Layout;
import org.apache.commons.io.FilenameUtils;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LOpenStreetMapLayer;
import org.vaadin.addon.leaflet.LeafletClickEvent;
import org.vaadin.addon.leaflet.shared.Point;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class SalespersonEdit extends AbstractEditor<Salesperson> {

    private static final double DEFAULT_LATITUDE = 53.241505;
    private static final double DEFAULT_LONGITUDE = 50.221245;
    private static final double ZOOM_LEVEL = 12.0;

    private LMap map;
    private LMarker personMarker;
    private Point iconSize;
    private Point iconAnchor;

    @Inject
    private Image image;

    @Inject
    private VBoxLayout mapContainer;

    @Inject
    private Button photoClearButton;

    @Inject
    private DataSupplier dataSupplier;

    @Inject
    private FileUploadField photoUploadField;

    @Inject
    private FileUploadingAPI fileUploadingAPI;

    @Override
    public void init(Map<String, Object> params) {
        iconSize = new Point(65, 65);
        iconAnchor = new Point(32, 32);
        photoUploadField.addFileUploadSucceedListener(this::photoUploadSucceedListener);
    }

    @Override
    protected void initNewItem(Salesperson item) {
        item.setLongitude(DEFAULT_LONGITUDE);
        item.setLatitude(DEFAULT_LATITUDE);
    }

    @Override
    public void ready() {
        initMap();
        initPersonMarker();
        addMapToContainer();
        displayPersonPhoto();
    }

    private void initMap() {
        map = new LMap();
        map.setZoomLevel(ZOOM_LEVEL);
        map.setCenter(getPersonLocation());
        map.addLayer(new LOpenStreetMapLayer());
        map.addClickListener(this::mapClickListener);
    }

    private void mapClickListener(LeafletClickEvent event) {
        Point salespersonLocation = event.getPoint();
        personMarker.setPoint(salespersonLocation);
        setPersonLocation(salespersonLocation);
    }

    private void initPersonMarker() {
        personMarker = new LMarker();
        personMarker.setPoint(getPersonLocation());
        personMarker.addDragEndListener(this::markerDragEndListener);
        map.addComponent(personMarker);
        updateMarkerIcon();
    }

    private Point getPersonLocation() {
        return new Point(getItem().getLatitude(), getItem().getLongitude());
    }

    private void markerDragEndListener(LMarker.DragEndEvent event) {
        Point personLocation = ((LMarker) event.getSource()).getPoint();
        setPersonLocation(personLocation);
    }

    private void setPersonLocation(Point location) {
        getItem().setLatitude(location.getLat());
        getItem().setLongitude(location.getLon());
    }

    private void addMapToContainer() {
        Layout layout = (Layout) WebComponentsHelper.unwrap(mapContainer);
        layout.addComponent(map);
    }

    private void photoUploadSucceedListener(FileUploadField.FileUploadSucceedEvent event) {
        resizePhoto();
        putPhotoIntoStorage();
        updateMarkerIcon();
        displayPersonPhoto();
    }

    private void resizePhoto() {
        File photoFile = fileUploadingAPI.getFile(photoUploadField.getFileId());
        if (Objects.nonNull(photoFile)) {
            try {
                BufferedImage photo = ImageIO.read(photoFile);
                photo = cropImage(photo);
                String fileExtension = FilenameUtils.getExtension(photoUploadField.getFileName());
                ImageIO.write(photo, fileExtension, photoFile);
            } catch (IOException ex) {
                throw new RuntimeException("Error reading/writing file to temporary storage", ex);
            }
        }
    }

    private BufferedImage cropImage(BufferedImage image) {
        int h = image.getHeight();
        int w = image.getWidth();

        if (h > w) {
            return image.getSubimage(0, (h - w) / 2, w, w);
        } else if (h < w) {
            return image.getSubimage((w - h) / 2, 0, h, h);
        } else {
            return image;
        }
    }

    private void putPhotoIntoStorage() {
        FileDescriptor photoDescriptor = photoUploadField.getFileDescriptor();

        try {
            fileUploadingAPI.putFileIntoStorage(photoUploadField.getFileId(), photoDescriptor);
        } catch (FileStorageException ex) {
            throw new RuntimeException("Error saving file to FileStorage", ex);
        }

        getItem().setPhoto(dataSupplier.commit(photoDescriptor));
    }

    private void updateMarkerIcon() {
        Resource personPhotoResource = null;

        FileDescriptor photoDescriptor = getItem().getPhoto();
        if (Objects.nonNull(photoDescriptor)) {
            WebFileDescriptorResource fileDescriptorResource = new WebFileDescriptorResource();
            fileDescriptorResource.setFileDescriptor(photoDescriptor);
            personPhotoResource = fileDescriptorResource.getResource();
        }

        personMarker.setIcon(personPhotoResource);
        personMarker.setIconSize(iconSize);
        personMarker.setIconAnchor(iconAnchor);
    }

    public void removePhoto() {
        getItem().setPhoto(null);
        updateMarkerIcon();
        displayPersonPhoto();
    }

    private void displayPersonPhoto() {
        FileDescriptor photoDescriptor = getItem().getPhoto();
        if (Objects.nonNull(photoDescriptor)) {
            image.setSource(FileDescriptorResource.class).setFileDescriptor(photoDescriptor);
            setImageComponentsActive(true);
        } else {
            setImageComponentsActive(false);
        }
    }

    private void setImageComponentsActive(Boolean value) {
        image.setVisible(value);
        photoClearButton.setEnabled(value);
    }
}