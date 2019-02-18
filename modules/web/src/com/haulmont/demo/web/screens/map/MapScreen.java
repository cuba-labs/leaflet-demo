package com.haulmont.demo.web.screens.map;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.VBoxLayout;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.cuba.web.gui.components.WebFileDescriptorResource;
import com.haulmont.demo.entity.Salesperson;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import org.vaadin.addon.leaflet.LLayerGroup;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LOpenStreetMapLayer;
import org.vaadin.addon.leaflet.shared.Point;
import org.vaadin.addon.leaflet.shared.PopupState;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MapScreen extends AbstractWindow {

    private static final double DEFAULT_LATITUDE = 53.241505;
    private static final double DEFAULT_LONGITUDE = 50.221245;
    private static final double ZOOM_LEVEL = 12.0;

    private LMap map;
    private PopupState popupState;
    private String popupTemplate;
    private LLayerGroup personMarkers;
    private int currentPersonIndex;
    private Point iconSize;
    private Point iconAnchor;
    private Point popupAnchor;

    @Inject
    private CollectionDatasource<Salesperson, UUID> salespersonsDs;

    @Inject
    private Button flyToNextPersonButton;

    @Inject
    private VBoxLayout mapContainer;

    @Override
    public void init(Map<String, Object> params) {
        personMarkers = new LLayerGroup();
        popupTemplate = createPopupTemplate();
        popupState = createPopupState();
        iconSize = new Point(65, 65);
        iconAnchor = new Point(35, 35);
        popupAnchor = new Point(0, -32);
        currentPersonIndex = -1;
    }

    /**
     * Creates localized popup template.
     *
     * @return popup template
     */
    private String createPopupTemplate() {
        return "<center>" +
                "<strong>%s</strong></br>" +
                getMessage("popup.Phone") + ": %s</br>" +
                getMessage("popup.Lat") + ": %.6f</br>" +
                getMessage("popup.Lon") + ": %.6f</br>" +
                "</center>";
    }

    /**
     * Creates common {@link PopupState} for all markers.
     *
     * @return {@link PopupState}
     */
    private PopupState createPopupState() {
        PopupState popupState = new PopupState();
        popupState.autoClose = false;
        popupState.closeOnClick = false;
        return popupState;
    }

    @Override
    public void ready() {
        initMap();
        drawPersonMarkers();
        addMapToContainer();
        flyToFirstPersonIfExists();
    }

    /**
     * Creates the map.
     * Sets default center of the map and default zoom level.
     * Adds a layer that uses OSM tiles.
     */
    private void initMap() {
        map = new LMap();
        map.setZoomLevel(ZOOM_LEVEL);
        map.addLayer(new LOpenStreetMapLayer());
        map.setCenter(new Point(DEFAULT_LATITUDE, DEFAULT_LONGITUDE));
    }

    /**
     * Creates personMarkers that contains map layers (markers in our case) to handle them as one in our code.
     * Refreshes salespersons DataSource.
     * For each Salesperson creates the marker that will be added to the personMarkers.
     * Finally adds personMarkers to the map.
     */
    private void drawPersonMarkers() {
        map.removeComponent(personMarkers);
        personMarkers = new LLayerGroup();
        salespersonsDs.refresh();

        for (Salesperson person : salespersonsDs.getItems()) {
            LMarker personMarker = createPersonMarker(person);
            personMarkers.addComponent(personMarker);
        }

        map.addComponent(personMarkers);
    }

    /**
     * Creates marker for the passed to the method Salesperson.
     * Sets popup and icon if passed person hast it's own photo.
     *
     * @param person Salesperson passed to the method
     * @return Salesperson's marker that represents it's location
     */
    private LMarker createPersonMarker(Salesperson person) {
        Point personLocation = new Point(person.getLatitude(), person.getLongitude());
        LMarker personMarker = new LMarker(personLocation);

        Resource photoResource = getPersonPhotoResource(person);
        personMarker.setIcon(photoResource);
        personMarker.setIconSize(iconSize);
        personMarker.setIconAnchor(iconAnchor);

        String popup = getPersonPopup(person);
        personMarker.setPopup(popup);
        personMarker.setPopupAnchor(popupAnchor);
        personMarker.setPopupState(popupState);

        return personMarker;
    }

    /**
     * Transforms persons's photo that is {@link FileDescriptor} object to a {@link Resource} object needed to
     * set marker's icon.
     *
     * @param person {@link Salesperson} passed to the method
     * @return Vaadin {@link Resource} object that represents salesperson's photo
     */
    private Resource getPersonPhotoResource(Salesperson person) {
        Resource photoResource = null;
        FileDescriptor photoDescriptor = person.getPhoto();
        if (Objects.nonNull(photoDescriptor)) {
            WebFileDescriptorResource fileDescriptorResource = new WebFileDescriptorResource();
            fileDescriptorResource.setFileDescriptor(photoDescriptor);
            photoResource = fileDescriptorResource.getResource();
        }
        return photoResource;
    }

    /**
     * Creates popup for {@link Salesperson}.
     *
     * @param person {@link Salesperson} passed to the method.
     * @return popup
     */
    private String getPersonPopup(Salesperson person) {
        return String.format(
                popupTemplate,
                person.getName(),
                person.getPhone(),
                person.getLatitude(),
                person.getLongitude()
        );
    }

    /**
     * Adds {@link #map} to {@link VBoxLayout} container via {@link Layout}.
     */
    private void addMapToContainer() {
        Layout layout = (Layout) WebComponentsHelper.unwrap(mapContainer);
        layout.addComponent(map);
    }

    /**
     * Performs map fly behavior to the first marker in {@link #personMarkers} if it exists.
     */
    private void flyToFirstPersonIfExists() {
        if (personMarkers.getComponentCount() > 0) {
            flyToNextPersonButton.setEnabled(true);
            currentPersonIndex = -1;
            flyToNextPerson();
        } else {
            flyToNextPersonButton.setEnabled(false);
        }
    }

    /**
     * Performs map fly behavior to the next tracked marker.
     * Algorithm complexity is O(n).
     */
    public void flyToNextPerson() {
        currentPersonIndex = (currentPersonIndex + 1) % personMarkers.getComponentCount();
        Iterator<Component> markersIterator = personMarkers.iterator();
        LMarker personMarker = null;

        for (int i = -1; i < currentPersonIndex; ++i) {
            personMarker = (LMarker) markersIterator.next();
        }

        if (Objects.nonNull(personMarker)) {
            map.flyTo(personMarker.getPoint(), ZOOM_LEVEL);
        }
    }

    /**
     * Refreshes {@link #salespersonsDs}, redraws markers and flies to the first marker if it exists.
     */
    public void refresh() {
        drawPersonMarkers();
        flyToFirstPersonIfExists();
    }
}