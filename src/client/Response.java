package client;

public enum Response
{
    CT_SUCCESS  ("<createtracker succ>\n"),
    CT_FAIL     ("<createtracker fail>\n"),
    CT_EXISTS   ("<createtracker ferr>\n"),
    GET_INVAL   ("<GET invalid>\n");

    private final String resp;

    Response(String s)
    {
       resp = s;
    }

    public boolean equals(String other)
    {
        return (other == null)? false : resp.equals(other);
    }
    public static Response fromString(String text)
    {
        if (text != null)
        {
            for (Response b : Response.values())
            {
                if (text.equals(b.resp))
                {
                    return b;
                }
            }
        }
        return null;
    }
}