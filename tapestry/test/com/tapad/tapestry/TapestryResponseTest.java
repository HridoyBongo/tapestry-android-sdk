package com.tapad.tapestry;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

public class TapestryResponseTest {
    @Test
    public void should_parse_data() {
        TapestryResponse response = new TapestryResponse("{'data':{'a':['1','2'],'b':['3']}}");
        assertThat(response.getData().get("a"), hasItems("1", "2"));
        assertThat(response.getData().get("b"), hasItems("3"));
    }

    @Test
    public void should_parse_ids() {
        TapestryResponse response = new TapestryResponse("{'ids':{'a':['1']}}");
        assertThat(response.getIds().get("a"), hasItems("1"));
    }

    @Test
    public void should_parse_audiences() {
        TapestryResponse response = new TapestryResponse("{'audiences':['a','b']}");
        assertThat(response.getAudiences(), hasItems("a", "b"));
    }

    @Test
    public void should_parse_errors() {
        TapestryResponse response = new TapestryResponse("{'errors':[1|name|message]}");
        assertThat(response.getErrors(), hasItems(new TapestryError(1, "name", "message")));
    }

    @Test
    public void should_parse_platforms() {
        TapestryResponse response = new TapestryResponse("{'platforms':['a','b']}");
        assertThat(response.getPlatforms(), hasItems("a", "b"));
    }

    @Test
    public void should_parse_devices() {
        TapestryResponse response = new TapestryResponse("{'devices':[{'a':'1'},{'b':'2'}]}");
        assertThat(response.getDevices(), hasItems(new TapestryResponse("{'a':'1'}"), new TapestryResponse("{'b':'2'}")));
    }

    @Test
    public void should_be_return_empty_map_by_default() {
        TapestryResponse response = new TapestryResponse("{}");
        assertThat(response.getData().isEmpty(), equalTo(true));
    }
}
