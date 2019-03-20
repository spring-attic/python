/*
 * Copyright 2017 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.spring.io.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Turanski
 **/
public class Page {
	//Map<String,URI> links = new HashMap<>();
	Map<String, Object> links = new HashMap<>();
	Map<String,Object> images = new HashMap<>();

	public Page() {
		try {
			links.put("google", new URI("https://www.google.com"));
			links.put("yahoo", new URI("https://www.yahoo.com"));
			links.put("pivotal", new URI("https://www.pivotal.io"));
			links.put("spring", new URI("https://www.spring.io"));

			images.put("image1", "image1.gif");
			images.put("image2", "image2.gif");
			images.put("image3", "image3.gif");
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}

	public Map<String, Object> getLinks() {
		return links;
	}

	public void setLinks(Map<String, Object> links) {
		this.links = links;
	}

	public Map<String, Object> getImages() {
		return images;
	}

	public void setImages(Map<String, Object> images) {
		this.images = images;
	}
}
