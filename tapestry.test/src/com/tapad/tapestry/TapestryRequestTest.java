package com.tapad.tapestry;

import android.test.AndroidTestCase;

public class TapestryRequestTest extends AndroidTestCase {
    public void test_should_append_values_to_array_parameters() {
        assertEncodedEquals(new TapestryRequest()
                .addAudiences("e")
                .toQuery(), "ta_add_audiences=['e']");
    }

    public void test_should_append_values_to_map_parameters() {
        assertEncodedEquals(new TapestryRequest()
                .addData("a", "{1}")
                .addData("b", "2")
                .setData("c", "3")
                .userIds("d", "4")
                .typedDid("e", "5")
                .toQuery(), "ta_add_data={'b':'2','a':'{1}'}&ta_set_data={'c':'3'}&ta_user_ids={'d':'4'}&ta_typed_did={'e':'5'}");
    }

    public void test_should_append_single_value_parameters() {
        assertEncodedEquals(new TapestryRequest().strength(1).depth(2).partnerId("a")
                .toQuery(), "ta_strength=1&ta_depth=2&ta_partner_id=a");
    }

    public void test_should_create_empty_parameters() {
        assertEncodedEquals(new TapestryRequest().listDevices().toQuery(),
                "ta_list_devices=");
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
