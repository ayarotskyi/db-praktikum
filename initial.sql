DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

CREATE TABLE Filliale(
  FName VARCHAR(255),
  FStreet VARCHAR(255),
  FZip VARCHAR(10),
  PRIMARY KEY (FName, FStreet, FZip)
);


CREATE TABLE Product (
  ProductAsin VARCHAR(255) PRIMARY KEY,
  Title VARCHAR(255) NOT NULL,
  Salesrank INT NOT NULL,
  Bild VARCHAR(255)
);
CREATE INDEX product_title_idx ON Product(Title);

CREATE TABLE ProductInFilliale (
  ProductInFillialeId SERIAL PRIMARY KEY,
  FName VARCHAR(255) NOT NULL ,
  FStreet VARCHAR(255) NOT NULL ,
  FZip VARCHAR(10) NOT NULL ,
  ProductAsin VARCHAR(255) REFERENCES Product (ProductAsin) on delete cascade on update cascade ,
  IState VARCHAR(255) NOT NULL,
  Price DECIMAL(10, 2),
  Cur VARCHAR(10),
  Avail BOOLEAN NOT NULL,
  UNIQUE (ProductAsin, FName, FStreet, FZip, IState),
  FOREIGN KEY (FName, FStreet, FZip) REFERENCES Filliale (FName, FStreet, FZip) on delete cascade on update cascade
);
CREATE INDEX productinfilliale_fname_idx ON ProductInFilliale (FName);
CREATE INDEX productinfilliale_fstreet_idx ON ProductInFilliale (FStreet);
CREATE INDEX productinfilliale_fzip_idx ON ProductInFilliale (FZip);

CREATE TABLE Book (
  BookAsin VARCHAR(255) PRIMARY KEY,
  Releasedate DATE NOT NULL CHECK (Releasedate <= CURRENT_DATE),
  Pages INT NOT NULL,
  ISBN VARCHAR(255) NOT NULL,
  FOREIGN KEY (BookAsin) REFERENCES Product (ProductAsin) ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE DVD (
  DVDAsin VARCHAR(255) PRIMARY KEY,
  Format VARCHAR(255) NOT NULL,
  Runtime INT NOT NULL,
  RegionCode VARCHAR(255) NOT NULL,
  FOREIGN KEY (DVDAsin) REFERENCES Product (ProductAsin) ON DELETE CASCADE ON UPDATE CASCADE	

);


CREATE TABLE Music (
  MusicAsin VARCHAR(255) PRIMARY KEY,	
  Releasedate DATE NOT NULL CHECK (Releasedate <= CURRENT_DATE),
  FOREIGN KEY (MusicAsin) REFERENCES Product (ProductAsin) ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE ArtistToMusic (
  MusicAsin VARCHAR(255) REFERENCES Music (MusicAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  artistName VARCHAR(255),
  PRIMARY KEY (MusicAsin, artistName)
);
CREATE INDEX artisttomusic_artistname_idx ON ArtistToMusic (artistName);

CREATE TABLE CreatorToMusic (
  MusicAsin VARCHAR(255) REFERENCES Music (MusicAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  creatorName VARCHAR(255),
  PRIMARY KEY (MusicAsin, creatorName)
);
CREATE INDEX creatortomusic_creatorname_idx ON CreatorToMusic (creatorName);

CREATE TABLE LabelToMusic (
  MusicAsin VARCHAR(255) REFERENCES Music (MusicAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  labelName VARCHAR(255),
  PRIMARY KEY (MusicAsin, labelName)
);
CREATE INDEX labeltomusic_labelname_idx ON LabelToMusic (labelName);

CREATE TABLE TrackToMusic (
  MusicAsin VARCHAR(255) REFERENCES Music (MusicAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  trackName VARCHAR(255),
  PRIMARY KEY (MusicAsin, trackName)
);
CREATE INDEX tracktomusic_trackname_idx ON TrackToMusic (trackName);

CREATE TABLE AuthorToBook (
  BookAsin VARCHAR(255) REFERENCES Book (BookAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  authorName VARCHAR(255),
  PRIMARY KEY (BookAsin, authorName)
);


CREATE TABLE PublisherToBook (
  BookAsin VARCHAR(255) REFERENCES Book (BookAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  publisherName VARCHAR(255),
  PRIMARY KEY (BookAsin, publisherName)
);


CREATE TABLE ActorToDVD (
  DVDAsin VARCHAR(255) REFERENCES DVD (DVDAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  actorName VARCHAR(255),
  PRIMARY KEY (DVDAsin, actorName)
);


CREATE TABLE CreatorToDVD (
  DVDAsin VARCHAR(255) REFERENCES DVD (DVDAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  creatorName VARCHAR(255),
  PRIMARY KEY (DVDAsin, creatorName)
);


CREATE TABLE DirectorToDVD (
  DVDAsin VARCHAR(255) REFERENCES DVD (DVDAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  directorName VARCHAR(255),
  PRIMARY KEY (DVDAsin, directorName)
);


CREATE TABLE Category (
  Id SERIAL PRIMARY KEY,
  categoryName VARCHAR(255) NOT NULL,
  parentCategory INTEGER REFERENCES Category(Id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX category_categoryname_idx ON Category (categoryName);
CREATE UNIQUE INDEX category_2col_uni_idx ON Category (categoryName, parentCategory)
WHERE parentCategory IS NOT NULL;
CREATE UNIQUE INDEX category_1col_uni_idx ON Category (categoryName)
WHERE parentCategory IS NULL;


CREATE TABLE ProductToCategory (
  ProductAsin VARCHAR(255) REFERENCES Product (ProductAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  categoryId INTEGER REFERENCES Category (id) ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY (ProductAsin, categoryId)
);
CREATE INDEX producttocategory_categoryid_idx ON ProductToCategory (categoryId);
CREATE INDEX producttocategory_productasin_idx ON ProductToCategory (ProductAsin);


CREATE TABLE SimilarProduct (
  Pnummer1 VARCHAR(255) REFERENCES Product (ProductAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  Pnummer2 VARCHAR(255) REFERENCES Product (ProductAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY (Pnummer1, Pnummer2),
  CHECK (Pnummer1 <> Pnummer2)
);
CREATE INDEX similarproduct_pnumer1_idx ON SimilarProduct (Pnummer1);

CREATE TABLE Kunde (
  Username VARCHAR(255) PRIMARY KEY,
  Kontonummer VARCHAR(255),
  Lieferadresse VARCHAR(255)
);
CREATE UNIQUE INDEX kunde_kontonummer_uni_idx ON Kunde (Kontonummer);

CREATE TABLE Kauf (
  Username VARCHAR(255) REFERENCES Kunde (Username) ON DELETE CASCADE ON UPDATE CASCADE,
  Datum TIMESTAMP NOT NULL CHECK (Datum <= CURRENT_TIMESTAMP),
  ProductInFillialeId SERIAL REFERENCES  ProductInFilliale(ProductInFillialeId) ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY (Username, Datum, ProductInFillialeId)
);
CREATE INDEX kauf_username_idx ON Kauf (Username);
CREATE INDEX kauf_pr_in_filliale_idx ON Kauf (ProductInFillialeId);


CREATE TABLE Feedback (
  Username VARCHAR(255) REFERENCES Kunde (Username) ON DELETE CASCADE ON UPDATE CASCADE,
  ProductAsin VARCHAR(255) REFERENCES Product (ProductAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  Rating INT NOT NULL CHECK (Rating >= 1 AND Rating <= 5),
  Helpful INT NOT NULL,
  fMessage TEXT NOT NULL,
  PRIMARY KEY (Username, ProductAsin)
);
CREATE INDEX feedback_username_idx ON Feedback (Username);
CREATE INDEX feedback_productasin_idx ON Feedback (ProductAsin);

CREATE TABLE GuestFeedback (
  Id SERIAL PRIMARY KEY,
  ProductAsin VARCHAR(255) REFERENCES Product (ProductAsin) ON DELETE CASCADE ON UPDATE CASCADE,
  Rating INT NOT NULL,
  Helpful INT NOT NULL,
  fMessage TEXT NOT NULL
);
CREATE INDEX guestfeedback_productasin_idx ON GuestFeedback (ProductAsin);

CREATE TABLE Error (
  Id SERIAL PRIMARY KEY,
  EntityName TEXT NOT NULL,
  ErrorMessage TEXT NOT NULL
);

--Calculating Rating
ALTER TABLE Product ADD COLUMN Rating DECIMAL(4,2);

CREATE OR REPLACE FUNCTION updateProductRating()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE Product
  SET Rating = (
    SELECT AVG(rating)
    FROM (
      SELECT rating
      FROM Feedback
      WHERE ProductAsin = NEW.ProductAsin
      UNION ALL
      SELECT rating
      FROM GuestFeedback
      WHERE ProductAsin = NEW.ProductAsin
    ) AS tmp
  )
  WHERE ProductAsin = NEW.ProductAsin;

  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_product_rating
    AFTER INSERT OR UPDATE OR DELETE ON Feedback
    FOR EACH ROW
EXECUTE FUNCTION updateProductRating();

CREATE TRIGGER update_product_rating_guest
    AFTER INSERT OR UPDATE OR DELETE ON GuestFeedback
    FOR EACH ROW
EXECUTE FUNCTION updateProductRating();




CREATE OR REPLACE FUNCTION deleteInvalidProducts()
    RETURNS VOID AS $$
BEGIN
    -- Delete products from Product table that are not present in Book, DVD, or Music
    DELETE FROM Product
    WHERE ProductAsin NOT IN (
        SELECT p.ProductAsin
        FROM Book b JOIN Product p ON b.BookAsin = p.ProductAsin
        UNION
        SELECT p.ProductAsin
        FROM DVD d JOIN Product p ON d.DVDAsin = p.ProductAsin
        UNION
        SELECT p.ProductAsin
        FROM Music m JOIN Product p ON m.MusicAsin = p.ProductAsin
    );

    -- Delete products from ProductInFilliale table that are not present in Book, DVD, or Music
    DELETE FROM ProductInFilliale
    WHERE ProductAsin NOT IN (
        SELECT p.ProductAsin
        FROM Book b JOIN Product p ON b.BookAsin = p.ProductAsin
        UNION
        SELECT p.ProductAsin
        FROM DVD d JOIN Product p ON d.DVDAsin = p.ProductAsin
        UNION
        SELECT p.ProductAsin
        FROM Music m JOIN Product p ON m.MusicAsin = p.ProductAsin
    );
END;
$$ LANGUAGE plpgsql;



