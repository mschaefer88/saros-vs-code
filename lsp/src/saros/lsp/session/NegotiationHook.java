package saros.lsp.session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import saros.negotiation.hooks.ISessionNegotiationHook;
import saros.negotiation.hooks.SessionNegotiationHookManager;
import saros.net.xmpp.JID;
import saros.preferences.IPreferenceStore;

public class NegotiationHook implements ISessionNegotiationHook {
    public NegotiationHook(SessionNegotiationHookManager mngr) {
        mngr.addHook(this);
    }

    @Override
    public String getIdentifier() {
        
        return UUID.randomUUID().toString();
    }

    @Override
    public void setInitialHostPreferences(IPreferenceStore hostPreferences) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<String, String> tellClientPreferences() {
        
        return new HashMap<>();
    }

    @Override
    public Map<String, String> considerClientPreferences(JID client, Map<String, String> input) {
        
        return new HashMap<>();
    }

    @Override
    public void applyActualParameters(Map<String, String> input, IPreferenceStore hostPreferences,
            IPreferenceStore clientPreferences) {
        

    }
}