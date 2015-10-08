/**
 * 
 */
package com.aidream.libthriftclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolUtil;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.transport.TIOStreamTransport;

/**
 * 通用结构体
 * 
 * @author HouKangxi
 * @date 2014年10月24日
 */
public class CommonStruct implements TBase<CommonStruct, TFieldIdEnum>, IStruct {
	private static final long serialVersionUID = -6430818557855795156L;
	private FieldInfo[] fields;
	private Object[] fieldValues;
	protected long __isset_bitfield = 0;
	protected TStruct struct;
	private Object structBean;

	public CommonStruct(FieldInfo[] fs, TStruct struct) {
		this.struct = struct;
		this.fields = fs;
		fieldValues = new Object[fs.length];
	}

	public CommonStruct(CommonStruct other) {
		if (other.fields != null) {
			fields = new FieldInfo[other.fields.length];
			System.arraycopy(other.fields, 0, fields, 0, fields.length);
		} else {
			fields = null;
		}
		__isset_bitfield = other.__isset_bitfield;
	}

	@Override
	public int compareTo(CommonStruct o) {
		long dis = __isset_bitfield - o.__isset_bitfield;
		if (dis != 0) {
			return (int) dis;
		}
		return Arrays.toString(fields).compareTo(Arrays.toString(o.fields));
	}

	@Override
	public void clear() {
		for (int i = 0; i < fieldValues.length; i++) {
			fieldValues[i] = null;
			setFieldsetIsSet(fields[i].getThriftFieldId(), false);
		}
	}

	@Override
	public TBase<CommonStruct, TFieldIdEnum> deepCopy() {
		return new CommonStruct(this);
	}

	@Override
	public TFieldIdEnum fieldForId(int id) {
		return getFieldInfoById(id);
	}

	@Override
	public boolean isSet(TFieldIdEnum field) {
		return EncodingUtils
				.testBit(__isset_bitfield, field.getThriftFieldId());
	}

	protected void setFieldsetIsSet(int thriftId, boolean isSet) {
		this.__isset_bitfield = EncodingUtils.setBit(this.__isset_bitfield,
				thriftId, isSet);
	}

	private int getFieldIndexById(int id) {
		return id - fields[0].getThriftFieldId();
	}

	protected FieldInfo getFieldInfoById(int id) {
		return fields[getFieldIndexById(id)];
	}

	@Override
	public void setFieldValue(TFieldIdEnum field, Object fieldValue) {
		int fieldIndex = getFieldIndexById(field.getThriftFieldId());
		fieldValues[fieldIndex] = fieldValue;
		if (structBean != null) {
			FieldInfo finfo = fields[fieldIndex];
			try {
				Field _field = structBean.getClass().getDeclaredField(
						finfo.getFieldName());
				_field.setAccessible(true);
				_field.set(structBean, fieldValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Object getFieldValue(TFieldIdEnum field) {
		return fieldValues[getFieldIndexById(field.getThriftFieldId())];
	}

	@Override
	public void read(TProtocol protocol) throws TException { 
		protocol.readStructBegin();
		Log.d(getClass().getSimpleName(), "read: fields="+Arrays.toString(fields));
		
		if (fields == null || fields.length < 1) {
			protocol.readStructEnd();
			return;
		}
		final short MIN_ID = fields[0].getThriftFieldId();
		final short MAX_ID = fields[fields.length - 1].getThriftFieldId();
		
		while (true) {
			TField localTField = protocol.readFieldBegin();
			if (localTField.type == Types.Stop.getTypeId()) {
				break;
			}
			short id = localTField.id;
			if (id < MIN_ID || id > MAX_ID) {
				TProtocolUtil.skip(protocol, localTField.type);
			} else {
				FieldInfo field = getFieldInfoById(id);

				if (localTField.type == field.getFieldDesc().type) {
					Types type = field.getFieldType();
					if (type != null) {
						ProtocolHandler protocolHandler = type
								.getProtocolHandler();
						if (protocolHandler != null) {
							Log.d(getClass().getSimpleName(), "read ,field= "+field);
							protocolHandler.read(protocol, field, this);
							setFieldsetIsSet(id, true);
						}
					}
				} else {
					TProtocolUtil.skip(protocol, localTField.type);
				}
			}
			protocol.readFieldEnd();
		}
		protocol.readStructEnd();
	}

	@Override
	public void write(TProtocol protocol) throws TException {
		if (struct == null) {
			throw new NullPointerException("struct is null!");
		}
//		Log.d(getClass().getSimpleName(), "write: fields="+Arrays.toString(fields)+", fieldValues="+Arrays.toString(fieldValues));
	    
		if (fields == null || fields.length < 1) {
			Log.d(getClass().getSimpleName(), "write: fields="+Arrays.toString(fields));
			protocol.writeStructBegin(struct);
			protocol.writeFieldStop();
			protocol.writeStructEnd();
			return;
		}
		Log.d(getClass().getSimpleName(), "write: length="+fields.length);
		for(FieldInfo f:fields){
			Log.d("field:", ""+f);
		}
		// copy bean fields to fieldValues[]
		if (structBean != null) {
			Class<?> structClass = structBean.getClass();
			try {
				int i = 0;
				for (FieldInfo f : fields) {
					Field _f = structClass.getDeclaredField(f.getFieldName());
					_f.setAccessible(true);
					fieldValues[i++] = _f.get(structBean);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// start Write
		protocol.writeStructBegin(struct);
		int i = 0;
		for (FieldInfo f : fields) {
			Types type = f.getFieldType();
			ProtocolHandler protocolHandler = type.getProtocolHandler();
			if (fieldValues[i] != null && protocolHandler != null) {
				protocol.writeFieldBegin(f.getFieldDesc());
				protocolHandler.write(protocol, f, this);
				protocol.writeFieldEnd();
			}
			i++;
		}
		protocol.writeFieldStop();
		protocol.writeStructEnd();
	}

	private void writeObject(ObjectOutputStream paramObjectOutputStream)
			throws IOException {
		try {
			write(new TCompactProtocol(new TIOStreamTransport(
					paramObjectOutputStream)));
		} catch (TException localTException) {
			throw new IOException(localTException);
		}
	}

	private void readObject(ObjectInputStream paramObjectInputStream)
			throws IOException, ClassNotFoundException {
		try {
			read(new TCompactProtocol(new TIOStreamTransport(
					paramObjectInputStream)));
		} catch (TException localTException) {
			throw new IOException(localTException);
		}
	}

	public void setFieldValues(Object[] fieldValues) {
		if (fieldValues != null && fieldValues.length > 0) {
			System.arraycopy(fieldValues, 0, this.fieldValues, 0,
					this.fieldValues.length);
		}
	}

	public FieldInfo[] getFields() {
		return fields;
	}

	public void setStructBean(Object structBean) {
		this.structBean = structBean;
	}

	public Object getStructBean() {
		return structBean;
	}

	public Object[] getFieldValues() {
		return fieldValues;
	}

}
