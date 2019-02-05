package eu.javaexperience.web;

public enum MIME
{
	html("text/html","html","htm"),
	css("text/css","css"),
	javascript("text/javascript","js"),
	plain("text/plain","text","txt"),
	json("application/json","json"),
	kep_png("image/png","png"),
	kep_gif("image/gif","gif"),
	kep_jpg("image/jpg","jpg"),
	kep_jpeg("image/jpeg","jpeg"),
	kep_bmp("image/bmp","bmp"),
	kep_svg("image/svg+xml","svg"),
	hang_mp3("audio/mpeg","mpeg"),
	hang_wav("audio/wav","wav"),
	video_mpeg("video/mpeg","mpeg"),
	alk_flash("video/x-flv","flv"),
	icon("image/x-icon", "ico"),
	stream("application/octet-stream");

	public final String mime;
	private final String[] fileExtensions;

	private MIME(String mime,String... fileExtensions)
	{
		this.mime = mime;
		this.fileExtensions = fileExtensions;
	}

	public static MIME recogniseFileExtension(String name)
	{
		if(name==null)
			return null;

		int index = name.lastIndexOf('.');

		if(index==-1)
			return stream;

		index++;

		name = name.substring(index);

		MIME[] mind = values();

		for(int i=0;i<mind.length;i++)
			for(int j=0;j<mind[i].fileExtensions.length;j++)
				if(mind[i].fileExtensions[j].equalsIgnoreCase(name))
					return mind[i];

		return stream;
	}
}