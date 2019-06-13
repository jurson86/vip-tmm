package com.tuandai.transaction.client.service.inf;

import com.tuandai.transaction.client.bo.EventDefinition;

public interface LogAnalyzerService {

    EventDefinition analysis(EventDefinition eventDefinition);

}
