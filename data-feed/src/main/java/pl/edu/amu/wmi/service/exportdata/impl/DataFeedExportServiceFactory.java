package pl.edu.amu.wmi.service.exportdata.impl;

import exception.DataFeedConfigurationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.exportdata.DataFeedExportService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataFeedExportServiceFactory {

    @Autowired
    private List<DataFeedExportService> services;

    private static final Map<DataFeedType, DataFeedExportService> dataFeedServiceCache = new HashMap<>();

    @PostConstruct
    public void initServiceCache() {
        for (DataFeedExportService service : services) {
            dataFeedServiceCache.put(service.getType(), service);
        }
    }

    public static DataFeedExportService getService(DataFeedType type) throws DataFeedConfigurationException {
        DataFeedExportService service = dataFeedServiceCache.get(type);
        if (service == null) {
            throw new DataFeedConfigurationException("Error during getting the DataFeedExportService");
        }
        return service;
    }

}
