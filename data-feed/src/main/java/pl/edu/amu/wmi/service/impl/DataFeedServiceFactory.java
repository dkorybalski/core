package pl.edu.amu.wmi.service.impl;

import exception.DataFeedConfigurationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.DataFeedImportService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataFeedServiceFactory {

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
            throw new DataFeedConfigurationException("Error during DataFeedImportService");
        }
        return service;
    }

}
