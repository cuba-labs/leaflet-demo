package com.haulmont.demo.core.listeners;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileLoader;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.listener.BeforeDeleteEntityListener;
import com.haulmont.demo.entity.Salesperson;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Objects;

@Component("leafletdemo_SalespersonListener")
public class SalespersonListener implements BeforeDeleteEntityListener<Salesperson> {

    @Inject
    private FileLoader fileLoader;

    @Override
    public void onBeforeDelete(Salesperson entity, EntityManager entityManager) {
        FileDescriptor photoDescriptor = entity.getPhoto();
        if (Objects.nonNull(photoDescriptor)) {
            try {
                fileLoader.removeFile(photoDescriptor);
            } catch (FileStorageException ex) {
                throw new RuntimeException("Error removing file from FileStorage", ex);
            }
            entityManager.remove(photoDescriptor);
        }
    }
}
