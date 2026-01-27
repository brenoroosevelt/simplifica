package com.simplifica.infrastructure.hibernate;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Base class for PostgreSQL enum types.
 * Handles the conversion between Java enums and PostgreSQL custom enum types.
 *
 * @param <E> the enum type
 */
public abstract class PostgreSQLEnumType<E extends Enum<E>> implements UserType<E> {

    private final Class<E> enumClass;
    private final String pgTypeName;

    protected PostgreSQLEnumType(Class<E> enumClass, String pgTypeName) {
        this.enumClass = enumClass;
        this.pgTypeName = pgTypeName;
    }

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<E> returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(E x, E y) {
        return x == y;
    }

    @Override
    public int hashCode(E x) {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public E nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        String value = rs.getString(position);
        if (rs.wasNull()) {
            return null;
        }
        return Enum.valueOf(enumClass, value);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, E value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value.name(), Types.OTHER);
        }
    }

    @Override
    public E deepCopy(E value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(E value) {
        return value;
    }

    @Override
    public E assemble(Serializable cached, Object owner) {
        return (E) cached;
    }

    @Override
    public E replace(E detached, E managed, Object owner) {
        return detached;
    }
}
