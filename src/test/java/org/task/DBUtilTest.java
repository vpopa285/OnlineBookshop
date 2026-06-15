package org.task;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.task.util.DBUtil;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


class DBUtilTest {
    @ParameterizedTest
    @MethodSource("dbUtilFields")
    void fieldsAreNotNull(String value) {
        assertThat(value).isNotNull();
    }

    static Stream<String> dbUtilFields() {
        return Stream.of(DBUtil.URL, DBUtil.USER, DBUtil.PASSWORD);
    }
}
