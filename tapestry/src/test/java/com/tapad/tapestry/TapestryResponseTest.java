package com.tapad.tapestry;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

public class TapestryResponseTest {
    @Test
    public void should_analytics() {
        TapestryResponse response = new TapestryResponse("{'analytics':{a:['1'],b:'2',c:3}}");
        assertThat(response.getAnalytics("b"), equalTo("2"));
        assertThat(response.getAnalytics("c"), equalTo("3"));
    }

    @Test
    public void should_parse_data() {
        TapestryResponse response = new TapestryResponse("{'data':{'a':['1','2'],'b':['3']}}");
        assertThat(response.getData("a"), hasItems("1", "2"));
        assertThat(response.getData("b"), hasItems("3"));
        assertTrue(response.getData("c").isEmpty());
    }

    @Test
    public void should_parse_ids() {
        TapestryResponse response = new TapestryResponse("{'ids':{'a':['1']}}");
        assertThat(response.getIds("a"), hasItems("1"));
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
}
