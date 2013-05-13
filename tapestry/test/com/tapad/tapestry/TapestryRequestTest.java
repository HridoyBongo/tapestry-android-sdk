package com.tapad.tapestry;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TapestryRequestTest {
    @Test
    public void should_append_values_to_array_parameters() {
        assertEncodedEquals(new TapestryRequest()
                .getData("a")
                .getData("b", "c")
                .getIds("d")
                .addAudiences("e")
                .toQuery(), "ta_get_data=['a','b','c']&ta_get_ids=['d']&ta_add_audiences=['e']");
    }

    @Test
    public void should_append_values_to_map_parameters() {
        assertEncodedEquals(new TapestryRequest()
                .addData("a", "{1}")
                .addData("b", "2")
                .setData("c", "3")
                .userIds("d", "4")
                .typedDid("e", "5")
                .toQuery(), "ta_add_data={'b':'2','a':'{1}'}&ta_set_data={'c':'3'}&ta_user_ids={'d':'4'}&ta_typed_did={'e':'5'}");
    }

    @Test
    public void should_append_single_value_parameters() {
        assertEncodedEquals(new TapestryRequest().strength(1).depth(2).partnerId("a")
                .toQuery(), "ta_strength=1&ta_depth=2&ta_partner_id=a");
    }

    @Test
    public void should_create_empty_parameters() {
        assertEncodedEquals(new TapestryRequest().getData().getPlatforms().getAudiences().listDevices().toQuery(),
                "ta_get_data=[]&ta_get_platforms=&ta_get_audiences=&ta_list_devices=");
    }

    private static void assertEncodedEquals(String actual, String expected) {
        assertEquals(expected
                .replaceAll("\\{", "%7B")
                .replaceAll(":", "%3A")
                .replaceAll("\\}", "%7D")
                .replaceAll("'", "%22")
                .replaceAll(",", "%2C")
                .replaceAll("\\[", "%5B")
                .replaceAll("\\]", "%5D"),
                actual
        );
    }
}
