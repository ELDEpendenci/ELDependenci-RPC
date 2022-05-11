package org.eldependenci.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Named;

public record JsonMapperFactory(ObjectMapper jsonMapper) {

    @Inject
    public JsonMapperFactory(@Named("eld-json") ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;

        // customize here?

    }
}
