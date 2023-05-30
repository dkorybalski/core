package pl.edu.amu.wmi.service.impl;

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

    public static final DataFeedImportService getService(DataFeedType type) {
        DataFeedImportService service = dataFeedServiceCache.get(type);
        if (service == null) {
            // TODO: 5/12/2023 add exception 
            throw new RuntimeException("");
        }
        return service;
    }

}
