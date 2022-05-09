package tutorial.showcase;

import java.util.HashMap;
import java.util.Map;

public class MyExampleInstallationImpl implements MyExampleInstallation{

    private final Map<String, String> stringMap = new HashMap<>();

    @Override
    public void putSomeValue(String key, String value) {
        this.stringMap.put(key, value);
    }

    public Map<String, String> getStringMap() {
        return stringMap;
    }
}
