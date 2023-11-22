package pl.edu.amu.wmi.service.importdata.impl;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.exception.DataFeedConfigurationException;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.importdata.DataFeedImportService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataFeedImportServiceFactory {

    @Autowired
    private List<DataFeedImportService> services;

    private static final Map<DataFeedType, DataFeedImportService> dataFeedServiceCache = new HashMap<>();

    @PostConstruct
    public void initServiceCache() {
        for (DataFeedImportService service : services) {
            dataFeedServiceCache.put(service.getType(), service);
        }
    }

    public static DataFeedImportService getService(DataFeedType type) throws DataFeedConfigurationException {
        DataFeedImportService service = dataFeedServiceCache.get(type);
        if (service == null) {
            throw new DataFeedConfigurationException("Error during getting DataFeedImportService");
        }
        return service;
    }

}
