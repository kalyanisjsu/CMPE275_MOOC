import socket
import comm_pb2
import struct

class SamplePy:

    def __init__(self):
        self.host = ''
        self.port = ''
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        while(self.host == '' and self.port == ''):

            print('Enter Valid host and port')
            #host = raw_input ( "host: " )
            #port = raw_input ( "port: " )
            self.port = "5570"
            #client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.client_socket.connect(("localhost", int(self.port)))

            voting = comm_pb2.InitVoting()
            #voting.voting_id = "1"

            header = comm_pb2.Header()
            header.tag = 'Voting'
            header.originator = 'client'
            header.routing_id = 2

            payload = comm_pb2.Payload()
            payload.init_voting.MergeFrom(voting)

            request = comm_pb2.Request()
            request.body.MergeFrom(payload)
            request.header.MergeFrom(header)

            print 'the byte size is'
            print request.ByteSize()

            success = self.write(request.SerializeToString())

            #received = self.read()
            requestResponse = comm_pb2.Request()
            received = self.client_socket.recv(60000)
            requestResponse.ParseFromString(received)
            #received = client_socket.recv(60000)
            #success = client_socket.send("Hit")
            #data = client_socket.listen();# to do listen
            print success
            print requestResponse
            self.client_socket.close()

    def write(self,bytestream):
        streamLen = struct.pack('>L', len(bytestream))
        framedStream = streamLen + bytestream
        try:
          self.client_socket.sendall(framedStream)
        except socket.error:
          self.close()
          raise Exception("socket send fail, close")

    def read(self):
        '''
        Read a byte stream message prepended by its length
        in 4 bytes in Big Endian from channel.
        The message content is returned.
        '''
        lenField = self.readnbytes(4)
        length = struct.unpack('>L', lenField)[0]
        stream = self.readnbytes(length)
        return stream

    def readnbytes(self, n):
        buf = ''
        while n > 0:
          data = self.client_socket.recv(n)
          if data == '':
            raise Exception("socket broken or connection closed")
          buf += data
          n -= len(data)
        return buf


    #if (data <> 'Q' and data <> 'q'):
    #   client_socket.send(data)
    #else:
    #    client_socket.send(data)
    def main(self):
        self.__init__()

if __name__ == "__main__":
    sam = SamplePy()
    sam.main()



