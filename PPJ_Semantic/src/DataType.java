
public enum DataType {
    VOID, CHAR, INT, CONST_CHAR, CONST_INT, CHAR_ARRAY, INT_ARRAY, CONST_CHAR_ARRAY, CONST_INT_ARRAY;

    public static DataType getDataType(String dataType) {

        dataType = dataType.toLowerCase();

        if (dataType.equals("void")) {
            return VOID;
        } else if (dataType.equals("char")) {
            return CHAR;
        } else if (dataType.equals("int")) {
            return INT;
        }

        return VOID;
    }

    /**
     * Returns type without array modificator.
     *
     * @return Type without array modificator.
     */
    public DataType removeArray() {
        if (!isArray())
            return this;

        if (getPlainType().equals(INT)) {
            if (isConst())
                return CONST_INT;
            else
                return INT;
        } else {
            if (isConst())
                return CONST_CHAR;
            else
                return CHAR;
        }
    }

    public DataType toArray() {
        if (this.equals(CHAR)) {
            return CHAR_ARRAY;
        } else if (this.equals(INT)) {
            return INT_ARRAY;
        } else if (this.equals(CONST_CHAR)) {
            return CONST_CHAR_ARRAY;
        }

        return CONST_INT_ARRAY;
    }

    public DataType getPlainType() {
        String type = toString();

        boolean _isConst = isConst();
        boolean _isArray = isArray();

        String res;

        if (!_isConst && !_isArray) {
            res = type;
        } else if (_isConst && _isArray) {
            res = type.split("_")[1];
        } else if (_isConst && !_isArray) {
            res = type.split("_")[1];
        } else {
            res = type.split("_")[0];
        }

        return getDataType(res);
    }

    public boolean isPlain() {
        return !isConst() && !isArray();
    }

    public boolean isConst() {
        return this.toString().contains("CONST");
    }

    public boolean isArray() {
        return this.toString().contains("ARRAY");
    }

    /**
     * Cast operator explicit.
     *
     * @param other
     * @return
     */
    public boolean explicit(DataType other) {
        if ((toString().equals("CONST_INT") || toString().equals("INT"))
                && (other.toString().equals("INT") || other.toString().equals("CONST_INT")
                || (other.toString().equals("CHAR") || other.toString().equals("CONST_CHAR")))) {
            return true;
        } else if ((toString().equals("CONST_CHAR") || toString().equals("CHAR"))
                && (other.toString().equals("CHAR") || other.toString().equals("CONST_CHAR")
                || other.toString().equals("INT") || other.toString().equals("CONST_INT"))) {
            return true;
        } else if (toString().equals("CHAR_ARRAY")
                && (other.toString().equals("CHAR_ARRAY") || other.toString().equals("CONST_CHAR_ARRAY"))) {
            return true;
        } else if (toString().equals("INT_ARRAY")
                && (other.toString().equals("INT_ARRAY") || other.toString().equals("CONST_INT_ARRAY")
                || other.toString().equals("CHAR_ARRAY") || other.toString().equals(CONST_CHAR_ARRAY))) {
            return true;
        }
        return false;
    }

    public boolean implicit(DataType other) {
        // String thisT = this.getPlainType();
        // String otherT = other.getPlainType();
        //
        // boolean case1 = (!isArray() && !other.isArray()) &&
        // (thisT.equals(otherT))
        // && ((isConst() && !other.isConst()) || (!isConst() &&
        // other.isConst()));
        // boolean case2 = !isArray() && !other.isArray() &&
        // thisT.equals("CHAR") && otherT.equals("INT");
        // boolean case3 = isArray() && !isConst() && other.isArray() &&
        // other.isConst() && thisT.equals(otherT);
        //
        // return case1 || case2 || case3;

        if (toString().equals(other.toString())) {
            return true;
        } else if ((toString().equals("CONST_INT") || toString().equals("INT"))
                && (other.toString().equals("INT") || other.toString().equals("CONST_INT"))) {
            return true;
        } else if ((toString().equals("CONST_CHAR") || toString().equals("CHAR"))
                && (other.toString().equals("CHAR") || other.toString().equals("CONST_CHAR")
                || other.toString().equals("INT") || other.toString().equals("CONST_INT"))) {
            return true;
        } else if (toString().equals("CHAR_ARRAY")
                && (other.toString().equals("CHAR_ARRAY") || other.toString().equals("CONST_CHAR_ARRAY"))) {
            return true;
        } else if (toString().equals("INT_ARRAY")
                && (other.toString().equals("INT_ARRAY") || other.toString().equals("CONST_INT_ARRAY"))) {
            return true;
        }
        return false;
    }
}
