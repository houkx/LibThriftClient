/**
 * 
 */
package com.aidream.libthriftclient;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TField;

/**
 * 通用字段描述
 * 
 * @author HouKangxi
 * @date 2014年10月24日
 */
public final class FieldInfo implements TFieldIdEnum {

	private String fieldName;
	private short thriftFieldId;
	private TField fieldDesc;
	private Class<?> fieldClass;
	private FieldInfo[] componentField;
	private Types fieldType;

	public FieldInfo() {
		super();
	}

	public FieldInfo(String fieldName, Types fieldType, short thriftFieldId) {
		this.thriftFieldId = thriftFieldId;
		this.fieldType = fieldType;
		this.fieldName = fieldName;
		fieldDesc = new TField(fieldName, fieldType.getTypeId(), thriftFieldId);
	}

	public String getFieldName() {
		return fieldName;
	}

	public FieldInfo setFieldName(String fieldName) {
		this.fieldName = fieldName;
		return this;
	}

	public short getThriftFieldId() {
		return thriftFieldId;
	}

	public FieldInfo setThriftFieldId(short thriftFieldId) {
		this.thriftFieldId = thriftFieldId;
		return this;
	}

	public TField getFieldDesc() {
		return fieldDesc;
	}

	public Types getFieldType() {
		if (fieldType == null) {
			fieldType = Types.findByType(fieldDesc.type);
		}
		return fieldType;
	}

	public FieldInfo setFieldDesc(TField fieldDesc) {
		this.fieldDesc = fieldDesc;
		this.fieldType = Types.findByType(fieldDesc.type);
		return this;
	}

	public Class<?> getFieldClass() {
		return fieldClass;
	}

	public FieldInfo setFieldClass(Class<?> structClass) {
		this.fieldClass = structClass;
		return this;
	}

	public FieldInfo[] getComponentFields() {
		return componentField;
	}

	public FieldInfo getComponentField() {
		return componentField[0];
	}

	public FieldInfo setComponentFields(FieldInfo... componentField) {
		this.componentField = componentField;
		return this;
	}

	public static FieldInfo parseFieldForType(Type type, String fieldName,
			short thriftId) {
		FieldInfo rsfield = null;
		if (type instanceof Class) {
			Class<?> clazz = (Class<?>) type;
			Types _type = Types.findByClass(clazz);
			rsfield = new FieldInfo(fieldName, _type, thriftId)
					.setFieldClass(clazz);
		} else if (type instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) type;
			Types rtype = Types.findByClass((Class<?>) ptype.getRawType());
			rsfield = new FieldInfo(fieldName, rtype, thriftId);
			Type[] argtypes = ptype.getActualTypeArguments();
			FieldInfo efs[] = new FieldInfo[argtypes.length];
			int i = 0;
			for (Type argtype : argtypes) {
				efs[i++] = parseFieldForType(argtype, null, (short) 0);
			}
			rsfield.setComponentFields(efs);
		}
		return rsfield;
	}

	@Override
	public String toString() {
		return "FieldInfo [fieldName=" + fieldName + ", thriftFieldId="
				+ thriftFieldId + ", fieldClass=" + fieldClass + ", fieldType="
				+ fieldType + "]";
	}
	
}
