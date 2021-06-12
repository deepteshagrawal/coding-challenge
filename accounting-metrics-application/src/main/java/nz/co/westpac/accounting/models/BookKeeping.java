package nz.co.westpac.accounting.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

/**
 * Model to hold BookKeeping Information from external data file. This class structure got generated
 * from external webtool. Used @JsonProperty to map json file value to another field. This is done
 * to show that, the same can be done for another fields which starts from object.
 */
public class BookKeeping
{
    @JsonProperty(value = "object_category")
    public String category;
    public String connection_id;
    public String user;
    public Date object_creation_date;
    public List<Accounting> data;
    public String currency;
    public String object_origin_type;
    public String object_origin_category;
    public String object_type;
    public String object_class;
    public Date balance_date;

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getConnection_id()
    {
        return connection_id;
    }

    public void setConnection_id(String connection_id)
    {
        this.connection_id = connection_id;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public Date getObject_creation_date()
    {
        return object_creation_date;
    }

    public void setObject_creation_date(Date object_creation_date)
    {
        this.object_creation_date = object_creation_date;
    }

    public List<Accounting> getData()
    {
        return data;
    }

    public void setData(List<Accounting> data)
    {
        this.data = data;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getObject_origin_type()
    {
        return object_origin_type;
    }

    public void setObject_origin_type(String object_origin_type)
    {
        this.object_origin_type = object_origin_type;
    }

    public String getObject_origin_category()
    {
        return object_origin_category;
    }

    public void setObject_origin_category(String object_origin_category)
    {
        this.object_origin_category = object_origin_category;
    }

    public String getObject_type()
    {
        return object_type;
    }

    public void setObject_type(String object_type)
    {
        this.object_type = object_type;
    }

    public String getObject_class()
    {
        return object_class;
    }

    public void setObject_class(String object_class)
    {
        this.object_class = object_class;
    }

    public Date getBalance_date()
    {
        return balance_date;
    }

    public void setBalance_date(Date balance_date)
    {
        this.balance_date = balance_date;
    }
}
