package com.tapad.tapestry;

import android.test.AndroidTestCase;
import java.util.HashMap;
import java.util.Map;

public class TapestryResponseTest extends AndroidTestCase{
    public void test_should_analytics() {
        TapestryResponse response = new TapestryResponse("{'analytics':{a:'1',b:'2'}}");
        Map<String, String> map = new HashMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        assertEquals(response.analytics(), map);
    }

    public void test_should_parse_data() {
        TapestryResponse response = new TapestryResponse("{'data':{'a':['1','2'],'b':['3']}}");
        assertEquals(response.getData("a").get(0), "1");
        assertEquals(response.getData("a").get(1), "2");
        assertEquals(response.getData("b").get(0), "3");
        assertTrue(response.getData("c").isEmpty());
    }

    public void test_should_parse_ids() {
        TapestryResponse response = new TapestryResponse("{'ids':{'a':['1']}}");
        assertEquals(response.getIds("a").get(0), "1");
    }

    public void test_should_parse_audiences() {
        TapestryResponse response = new TapestryResponse("{'audiences':['a','b']}");
        assertEquals(response.getAudiences().get(0), "a");
        assertEquals(response.getAudiences().get(1), "b");
    }
    
    public void test_should_parse_errors() {
        TapestryResponse response = new TapestryResponse("{'errors':[1|name|message]}");
        assertEquals(response.getErrors().get(0), new TapestryError(1, "name", "message"));
    }
    
    public void test_should_parse_platforms() {
        TapestryResponse response = new TapestryResponse("{'platforms':['a','b']}");
        assertEquals(response.getPlatforms().get(0), "a");
        assertEquals(response.getPlatforms().get(1), "b");
    }

    public void test_should_parse_devices() {
        TapestryResponse response = new TapestryResponse("{'devices':[{'a':'1'},{'b':'2'}]}");
        assertEquals(response.getDevices().get(0), new TapestryResponse("{'a':'1'}"));
        assertEquals(response.getDevices().get(1), new TapestryResponse("{'b':'2'}"));
    }
}
