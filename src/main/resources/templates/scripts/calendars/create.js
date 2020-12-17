(function iefe() {
    <<common_functions>>

    let calendarData = <<calendar>>;
    try {
        calendarApp.Calendar(calendarData).make();
    } catch (error) {
    }
    return "";
})()
