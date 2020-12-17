(function iefe() {
    <<common_functions>>

    let calendarName = "<<calendarName>>";
    let summary = "<<summary>>";
    try {
        let calendar = getCalendarByName(calendarName);
        if (calendar !== undefined) {
            let eventList = calendar.events.whose({summary: {_beginsWith: summary}});
            return JSON.stringify(toJSONEvents(eventList));
        }
    } catch (error) {
    }
    return "";
})()
