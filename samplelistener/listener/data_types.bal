public type GenericEventWrapperEvent record {
    string token;
    string team_id;
    string api_app_id;
    EventType event;
    string 'type;
    string event_id;
    int event_time;
    string event_context?;
    json authorizations?;
};

public type EventType record {
    string 'type;
};

// Get verfication token from user
// @display {label: "Connection Config"}
// public type ListenerConfiguration record {
//     @display {label: "Verification Token"}
//     string verificationToken;
//      {| int x; json...; |}
// };
 