package com.aidream.libthriftclient;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSet;

/**
 * Thrift 类型枚举
 * 
 * @author HouKangxi
 * 
 */
public enum Types {

	Stop((byte) 0),

	Void((byte) 1, java.lang.Void.TYPE),

	Bool((byte) 2, new Class[] { boolean.class, Boolean.class },
			new ProtocolHandler() {
				public void read(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					struct.setFieldValue(field, protocol.readBool());
				}

				public void write(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					protocol.writeBool((Boolean) struct.getFieldValue(field));
				}
			}),

	Byte((byte) 3, new Class[] { byte.class, Byte.class },
			new ProtocolHandler() {
				public void read(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					struct.setFieldValue(field, protocol.readByte());
				}

				public void write(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					protocol.writeByte((Byte) struct.getFieldValue(field));
				}
			}),

	Double((byte) 4, new Class[] { double.class, Double.class },
			new ProtocolHandler() {
				public void read(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					struct.setFieldValue(field, protocol.readDouble());
				}

				public void write(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					protocol.writeDouble((Double) struct.getFieldValue(field));
				}
			}),

	I16((byte) 6, new Class[] { short.class, Short.class },
			new ProtocolHandler() {
				public void read(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					struct.setFieldValue(field, protocol.readI16());
				}

				public void write(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					protocol.writeI16((Short) struct.getFieldValue(field));
				}
			}),

	I32((byte) 8, new Class[] { int.class, Integer.class },
			new ProtocolHandler() {
				public void read(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					struct.setFieldValue(field, protocol.readI32());
				}

				public void write(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					protocol.writeI32((Integer) struct.getFieldValue(field));
				}
			}),

	I64((byte) 10, new Class[] { long.class, Long.class },
			new ProtocolHandler() {
				public void read(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					struct.setFieldValue(field, protocol.readI64());
				}

				public void write(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					protocol.writeI64((Long) struct.getFieldValue(field));
				}
			}),

	String((byte) 11, new Class[] { String.class, ByteBuffer.class },
			new ProtocolHandler() {
				public void read(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					if (field.getFieldClass() == ByteBuffer.class) {
						struct.setFieldValue(field, protocol.readBinary());
					} else {
						String rs = protocol.readString();
						Log.d("Type::String: readString()",rs);
						struct.setFieldValue(field, rs);
					}
				}

				public void write(TProtocol protocol, FieldInfo field,
						IStruct struct) throws TException {
					if (field.getFieldClass() == ByteBuffer.class) {
						protocol.writeBinary((ByteBuffer) struct
								.getFieldValue(field));
					} else {
						String rs = (String) struct
								.getFieldValue(field);
						Log.d("Type::String: writeString()",rs);
						protocol.writeString(rs);
					}
				}
			}),

	Struct((byte) 12, new StructProtocolHandler()),

	Map((byte) 13, Map.class, new ProtocolHandler() {
		public void read(TProtocol protocol, FieldInfo field, IStruct struct)
				throws TException {
			TMap localMap = protocol.readMapBegin();
			HashMap<Object, Object> map = new HashMap<Object, Object>(
					2 * localMap.size);
			struct.setFieldValue(field, map);
			FieldInfo[] eles = field.getComponentFields();
			FieldInfo element_key = eles[0];
			FieldInfo element_val = eles[1];
			Types tk = element_key.getFieldType();
			Types tv = element_val.getFieldType();
			for (int i = 0; i < localMap.size; i++) {
				TmpStruct tmpStruct = new TmpStruct();
				tk.protocolHandler.read(protocol, element_key, tmpStruct);
				Object key = tmpStruct.getFieldValue(null);
				tv.protocolHandler.read(protocol, element_val, tmpStruct);
				Object val = tmpStruct.getFieldValue(null);
				map.put(key, val);
			}
			protocol.readMapEnd();
		}

		public void write(TProtocol protocol, FieldInfo field, IStruct struct)
				throws TException {
			FieldInfo[] eles = field.getComponentFields();
			FieldInfo element_key = eles[0];
			FieldInfo element_val = eles[1];
			@SuppressWarnings("unchecked")
			Map<Object, Object> map = (Map<Object, Object>) struct
					.getFieldValue(field);
			final byte typeKey = element_key.getFieldDesc().type;
			final byte typeVal = element_val.getFieldDesc().type;
			Types tk = element_key.getFieldType();
			Types tv = element_val.getFieldType();
			protocol.writeMapBegin(new TMap(typeKey, typeVal, map.size()));
			for (@SuppressWarnings("rawtypes")
			Map.Entry entry : map.entrySet()) {
				tk.protocolHandler.write(protocol, element_key, new TmpStruct(
						entry.getKey()));
				tv.protocolHandler.write(protocol, element_val, new TmpStruct(
						entry.getValue()));
			}
			protocol.writeMapEnd();
		}
	}),

	Set((byte) 14, Set.class, new ProtocolHandler() {
		public void read(TProtocol protocol, FieldInfo field, IStruct struct)
				throws TException {
			TSet localSet = protocol.readSetBegin();
			HashSet<Object> set = new HashSet<Object>(localSet.size);
			struct.setFieldValue(field, set);
			for (int i = 0; i < localSet.size; i++) {
				FieldInfo element = field.getComponentField();
				Types t = element.getFieldType();
				TmpStruct tmpStruct = new TmpStruct();
				t.protocolHandler.read(protocol, element, tmpStruct);
				set.add(tmpStruct.getFieldValue(element));
			}
			protocol.readSetEnd();
		}

		public void write(TProtocol protocol, FieldInfo field, IStruct struct)
				throws TException {
			FieldInfo element = field.getComponentField();
			Set<?> set = (Set<?>) struct.getFieldValue(field);
			Types t = field.getFieldType();
			protocol.writeSetBegin(new TSet(element.getFieldDesc().type, set
					.size()));
			for (Object eo : set) {
				t.protocolHandler.write(protocol, element, new TmpStruct(eo));
			}
			protocol.writeSetEnd();
		}
	}),

	List((byte) 15, List.class, new ProtocolHandler() {
		public void read(TProtocol protocol, FieldInfo field, IStruct struct)
				throws TException {
			TList localTList = protocol.readListBegin();
			ArrayList<Object> list = new ArrayList<Object>(localTList.size);
			struct.setFieldValue(field, list);
			for (int i = 0; i < localTList.size; i++) {
				FieldInfo element = field.getComponentField();
				Types t = element.getFieldType();
				TmpStruct tmpStruct = new TmpStruct();
				t.protocolHandler.read(protocol, element, tmpStruct);
				list.add(tmpStruct.getFieldValue(element));
			}
			protocol.readListEnd();
		}

		public void write(TProtocol protocol, FieldInfo field, IStruct struct)
				throws TException {
			FieldInfo element = field.getComponentField();
			List<?> list = (List<?>) struct.getFieldValue(field);
			Types t = element.getFieldType();
			protocol.writeListBegin(new TList(element.getFieldDesc().type, list
					.size()));
			for (Object eo : list) {
				t.protocolHandler.write(protocol, element, new TmpStruct(eo));
			}
			protocol.writeListEnd();
		}
	}),

	Enum((byte) 16);

	private final byte typeId;
	private final Class<?>[] typeClasses;
	private final ProtocolHandler protocolHandler;

	private Types(byte typeId) {
		this(typeId, (Class<?>[]) null, null);
	}

	private Types(byte typeId, Class<?> typeClass) {
		this(typeId, typeClass, null);
	}

	private Types(byte typeId, ProtocolHandler handler) {
		this(typeId, (Class<?>[]) null, handler);
	}

	private Types(byte typeId, Class<?> typeClass, ProtocolHandler handler) {
		this(typeId, new Class[] { typeClass }, handler);
	}

	private Types(byte typeId, Class<?> typeClasses[], ProtocolHandler handler) {
		this.typeId = typeId;
		this.typeClasses = typeClasses;
		protocolHandler = handler;
	}

	public byte getTypeId() {
		return typeId;
	}

	public ProtocolHandler getProtocolHandler() {
		return protocolHandler;
	}

	private static final HashMap<Class<?>, Types> classTypeMap = new HashMap<Class<?>, Types>();
	private static final byte[] _KS;
	static {
		_KS = new byte[Types.values().length];
		int i = 0;
		for (Types t : Types.values()) {
			_KS[i++] = t.typeId;
			Class<?>[] classes = t.typeClasses;
			if (classes != null && classes.length > 0) {
				for (Class<?> tc : classes) {
					classTypeMap.put(tc, t);
				}
			}
		}
	}

	public static Types findByType(byte type) {
		return Types.values()[Arrays.binarySearch(_KS, type)];
	}

	public static Types findByClass(Class<?> jclass) {
		Types tp = classTypeMap.get(jclass);
		if (tp == null) {
			tp = Struct;// 不存在，则认为是结构体
		}
		return tp;
	}
}
