package com.openlibin.client.coder;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * The MIT License (MIT)
 * Copyright (c) 2009-2015 HONG LEIMING
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import java.util.ArrayList;
import java.util.List;

import com.openlibin.net.buffer.PooledBufferPool;
import com.openlibin.net.core.Codec;
import com.openlibin.net.core.IoBuffer;
import com.openlibin.net.core.Session;
import com.openlibin.redis.core.command.impl.RedisCommand;
import com.openlibin.redis.core.enums.ReplyState;
import com.openlibin.redis.core.enums.RequestState;
import com.openlibin.redis.core.enums.Type;
import com.openlibin.redis.core.reply.IRedisReply;
import com.openlibin.redis.core.reply.RedisConstants;
import com.openlibin.redis.core.reply.impl.BulkRedisReply;
import com.openlibin.redis.core.reply.impl.ErrorRedisReply;
import com.openlibin.redis.core.reply.impl.IntegerRedisReply;
import com.openlibin.redis.core.reply.impl.MultyBulkRedisReply;
import com.openlibin.redis.core.reply.impl.StatusRedisReply;

import io.netty.buffer.ByteBuf;

public class ClientRedisCodec implements Codec {
	@Override
	public IoBuffer encode(Object obj) {
		try {
			if (!(obj instanceof RedisCommand)) {
				throw new RuntimeException("Message unknown");
			}
			RedisCommand msg = (RedisCommand) obj;
			IoBuffer buf = PooledBufferPool.DEFAULT.getBuffer();/// IoBuffer.allocate(1024);

			msg.encodeNew(buf);

			buf.flip();
			return buf;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

 

	public void checkpoint(Session sess,ReplyState replyState) {
		sess.replyState = replyState;
	}

	@Override
	public Object decode(IoBuffer in,Session sess) {

		try {

			if (in.limit() < 1) {
				return null;
			}
			switch (sess.replyState) {
			case READ_INIT:
				// System.out.println("READ_INIT");
				char ch = (char) in.readByte();
				//System.out.println("READ_INIT " + ch);
				if (ch == RedisConstants.ASTERISK_BYTE) {
					sess.redisReply = new MultyBulkRedisReply();
				} else if (ch == RedisConstants.DOLLAR_BYTE) {
					sess.redisReply = new BulkRedisReply();
				} else if (ch == RedisConstants.COLON_BYTE) {
					sess.redisReply = new IntegerRedisReply();
				} else if (ch == RedisConstants.OK_BYTE) {
					sess.redisReply = new StatusRedisReply();
				} else if (ch == RedisConstants.ERROR_BYTE) {
					sess.redisReply = new ErrorRedisReply();
				}
				checkpoint(sess,ReplyState.READ_REPLY);
			case READ_REPLY:
				//System.out.println("READ_REPLY");

				Type type = sess.redisReply.getType();
				if (type == Type.INTEGER) {
					byte[] value = readLine(in, false).getBytes(RedisConstants.DEFAULT_CHARACTER);
					((IntegerRedisReply) sess.redisReply).setValue(value);
				} else if (type == Type.STATUS) {
					byte[] value = readLine(in, false).getBytes(RedisConstants.DEFAULT_CHARACTER);
					((StatusRedisReply) sess.redisReply).setValue(value);
				} else if (type == Type.ERROR) {
					byte[] value = readLine(in, false).getBytes(RedisConstants.DEFAULT_CHARACTER);
					((ErrorRedisReply) sess.redisReply).setValue(value);
				} else if (type == Type.BULK) {
					BulkRedisReply temp = readBulkReply(in, (BulkRedisReply) sess.redisReply, false);
					if (temp == null) {
						return null;
					}
				} else if (type == Type.MULTYBULK) {
					MultyBulkRedisReply temp = readArrayReply(in, (MultyBulkRedisReply) sess.redisReply);
					if (temp == null) {
						return null;
					}
				}
				// out.add(this.redisReply);
				IRedisReply temp = sess.redisReply;
				sess.redisReply = null;
				checkpoint(sess,ReplyState.READ_INIT);
				return temp;
			default:
				throw new Error("can't reach there!");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private MultyBulkRedisReply readArrayReply(IoBuffer buffer, MultyBulkRedisReply multyBulkRedisReply)
			throws Exception {
		if (multyBulkRedisReply.getCount() <= 0) {
			int count = readInt(buffer, false);
			multyBulkRedisReply.setCount(count);
		}
		int startIndex = 0;
		if (multyBulkRedisReply.getList() == null) {
			startIndex = 0;
		} else {
			startIndex = multyBulkRedisReply.getList().size();
		}
		for (int i = startIndex; i < multyBulkRedisReply.getCount(); i++) {
			buffer.mark();
			if (buffer.limit() < 1) {
				System.out.println("reset");
				buffer.reset();
				return null;
			}

			char type = (char) buffer.readByte();
			if (type == RedisConstants.COLON_BYTE) {
				IntegerRedisReply reply = new IntegerRedisReply();
				reply.setValue(readLine(buffer, true).getBytes(RedisConstants.DEFAULT_CHARACTER));
				multyBulkRedisReply.addReply(reply);
			} else if (type == RedisConstants.DOLLAR_BYTE) {
				BulkRedisReply bulkReply = new BulkRedisReply();
				readBulkReply(buffer, bulkReply, true);
				multyBulkRedisReply.addReply(bulkReply);
			}
		}
		if (multyBulkRedisReply.getCount() == multyBulkRedisReply.getList().size()) {
			// ok
			return multyBulkRedisReply;
		} else {
			return null;
		}
	}

	private BulkRedisReply readBulkReply(IoBuffer buffer, BulkRedisReply bulkReply, boolean isMark) throws Exception {
		if (!isMark) {
			buffer.mark();
		}
		int length = readInt(buffer, true);
		if (length == -2) {
			return null;
		}
		if (buffer.remaining() < length + 2) {
			buffer.reset();
			return null;
		}

		bulkReply.setLength(length);
		if (length == -1) {// read null

		} else if (length == 0) {// read ""
			buffer.skipBytes(2);
		} else {

			byte[] value = new byte[length];
			buffer.readBytes(value);
			bulkReply.setValue(value);
			buffer.skipBytes(2);
		}

		return bulkReply;
	}

	private int readInt(IoBuffer buffer, boolean isMark) {
		String s = readLine(buffer, isMark);
		if (s == null) {
			return 0;
		}
		return Integer.parseInt(s);
	}

	private String readLine(IoBuffer buffer, boolean isMark) {
		if (!isMark) {
			buffer.mark();
		}
		if (buffer.limit() < 1) {
			System.out.println("reset");
			buffer.reset();
			return null;
		}

		StringBuilder sb = new StringBuilder();
		char ch = (char) buffer.readByte();
		while (ch != RedisConstants.CR_BYTE) {
			sb.append(ch);
			if (buffer.limit() < 1) {
				buffer.reset();
				return null;
			}
			ch = (char) buffer.readByte();// \r
		}
		buffer.readByte();// \n
		return sb.toString();
	}

}