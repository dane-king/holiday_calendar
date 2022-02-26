import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static java.time.Month.*;
import static org.junit.jupiter.api.Assertions.*;

class HolidayBuilderTest {

    private Holidays holidays;

    @BeforeEach
    void setUp() {
        holidays = new Holidays.HolidaysBuilder()
                .withFederalHolidays()
                .forYears(2021,2022,2023,2024)
                .build();
    }

    @Test
    void shouldHaveDatesForCurrentYear() {
        holidays = new Holidays.HolidaysBuilder()
                .withFederalHolidays()
                .build();
        assertEquals(holidays.getAllDates().size(), 6);

    }

    @ParameterizedTest
    @MethodSource("provideDates")
    void isBlank_ShouldReturnTrueForNullOrBlankStringsVariableSource(
            LocalDate date, boolean expected) {
        assertEquals(expected, holidays.isHoliday(date));

    }
    private static Stream<Arguments> provideDates() {
        return Stream.of(
                Arguments.of(LocalDate.of(2022, DECEMBER,25), false),
                Arguments.of(LocalDate.of(2022, DECEMBER,26), true),
                Arguments.of(LocalDate.of(2022,JULY,4), true),
                Arguments.of(LocalDate.of(2021,JULY,5), true),
                Arguments.of(LocalDate.of(2022,JANUARY,1), false),
                Arguments.of(LocalDate.of(2023,JANUARY,1), false),
                Arguments.of(LocalDate.of(2023,JANUARY,2), true),
                Arguments.of(LocalDate.of(2021,DECEMBER,31), true),
                Arguments.of(LocalDate.of(2022,AUGUST,20), false),
                Arguments.of(LocalDate.of(2022,MAY,30), true),
                Arguments.of(LocalDate.of(2024,NOVEMBER,28), true)
        );
    }
}