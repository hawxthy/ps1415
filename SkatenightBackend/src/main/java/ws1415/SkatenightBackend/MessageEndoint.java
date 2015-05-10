package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;

/**
 * Created by Bernd on 04.05.2015.
 */
public class MessageEndoint extends SkatenightServerEndpoint {
    public void sendMessage(@Named("reciever") String reciever, @Named("content") String content){
        // Dummy
    }
}
