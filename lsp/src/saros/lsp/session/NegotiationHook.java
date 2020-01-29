package saros.lsp.session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import saros.negotiation.hooks.ISessionNegotiationHook;
import saros.negotiation.hooks.SessionNegotiationHookManager;
import saros.net.xmpp.JID;
import saros.preferences.IPreferenceStore;

public class NegotiationHook implements ISessionNegotiationHook {

  private static final Logger log = Logger
  .getLogger(NegotiationHook.class);

    public NegotiationHook(SessionNegotiationHookManager mngr) {

        log.info("adding hook");
        mngr.addHook(this);
    }

    @Override
    public String getIdentifier() {
        
        log.info("getIdentifier");
        return UUID.randomUUID().toString();
    }

    @Override
    public void setInitialHostPreferences(IPreferenceStore hostPreferences) {
        // TODO Auto-generated method stub
        
        log.info("setInitialHostPreferences");
    }

    @Override
    public Map<String, String> tellClientPreferences() {
        
        log.info("tellClientPreferences");
        return new HashMap<>();
    }

    @Override
    public Map<String, String> considerClientPreferences(JID client, Map<String, String> input) {
        
        log.info("considerClientPreferences");
        return new HashMap<>();
    }

    @Override
    public void applyActualParameters(Map<String, String> input, IPreferenceStore hostPreferences,
            IPreferenceStore clientPreferences) {
        

                log.info("applyActualParameters");
    }
}