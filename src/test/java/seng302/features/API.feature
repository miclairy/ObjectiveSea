Feature: Sending and understanding key presses

  Scenario: User presses enter key to tack their boat
    Given Sally has a boat
    When Sally presses the "Enter" key
    Then the heading of Sally's boat has been changed

  Scenario: User presses enter key to tack their boat
    Given Sally has a boat
    When Sally presses the "Shift" key
    Then the sails should be brought in so that the speed becomes 0
