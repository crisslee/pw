/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.goomeim.domain;

import com.goomeim.utils.GMCommonUtils;

import net.goome.im.chat.GMContact;

public class GMUser extends GMContact
{

	/**
	 * initial letter for nickname
	 */
	protected String initialLetter;
	/**
	 * avatar of the user
	 */
	protected String avatar;

	public GMUser(long username)
	{
		this.uid = username;
	}

	public String getInitialLetter()
	{
		if (initialLetter == null)
		{
			GMCommonUtils.setUserInitialLetter(this);
		}
		return initialLetter;
	}

	public void setInitialLetter(String initialLetter)
	{
		this.initialLetter = initialLetter;
	}

	public String getAvatar()
	{
		return avatar;
	}

	public void setAvatar(String avatar)
	{
		this.avatar = avatar;
	}

	@Override
	public int hashCode()
	{
		return 17 * getNickname().hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof GMUser))
		{
			return false;
		}
		return getUid() == ((GMUser) o).getUid();
	}

	@Override
	public String toString()
	{
		return getNickname() == null ? String.valueOf(getUid()) : getNickname();
	}
}
