package common.test;

import common.annotation.GetRequest;
import common.annotation.RequestURL;

@RequestURL(url = "/api")
public class TestService {

    @GetRequest(url = "/test")
    public void test() {

    }
}
