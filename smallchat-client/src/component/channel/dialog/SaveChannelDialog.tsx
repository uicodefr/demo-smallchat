import React from 'react';
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';
import { ChannelModel } from '../../../model/chat/channel.model';
import Form from 'react-bootstrap/Form';
import { Formik, FormikValues } from 'formik';
import * as yup from 'yup';

interface Props {
  show: boolean,
  channel: ChannelModel,
  onSave: (channel) => void,
  onCancel: () => void
}

interface State {
  validatedForm: boolean
}

export class SaveChannelDialog extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);

    this.state = {
      validatedForm: false
    };

    this.handleCancel = this.handleCancel.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  componentDidUpdate(prevProps) {
    if (prevProps.show !== this.props.show) {
      this.setState({
        validatedForm: false
      });
    }
  }

  handleCancel() {
    this.props.onCancel();
  }

  handleSubmit(formValues : FormikValues) {
    this.setState({
      validatedForm: true
    });
    const channelToSave = {...this.props.channel, ...formValues} as ChannelModel;
    this.props.onSave(channelToSave);
  }

  render() {
    const inCreationMode =  !this.props.channel.id;
    const title = inCreationMode ? 'Create Channel' : 'Edit Channel';
    const saveLabel = inCreationMode ? 'Create': 'Update';
    const valueChannel = { id: '', name: '', description: '', ...this.props.channel };
    valueChannel.description = valueChannel.description || '';

    const schema = yup.object({
      id: yup.string().required().matches(ChannelModel.ID_PATTERN),
      name: yup.string().required(),
      description: yup.string()
    });

    return (
      <Modal show={this.props.show} size="lg" onHide={this.handleCancel}>
        <Formik
          validationSchema={schema}
          onSubmit={this.handleSubmit}
          initialValues={valueChannel}
          >
          {({
            values,
            errors,
            handleChange,
            handleBlur,
            handleSubmit,
            isSubmitting
          }) => (
          <Form validated={this.state.validatedForm} onSubmit={handleSubmit}>

            <Modal.Header>
              <Modal.Title> {title} </Modal.Title>
            </Modal.Header>

            <Modal.Body>
              <Form.Group className="required" controlId="formId">
                <Form.Label>Id</Form.Label>
                <Form.Control type="text" name="id" placeholder="channelId" required pattern={ChannelModel.ID_PATTERN}
                  value={values.id} onChange={handleChange} onBlur={handleBlur} isInvalid={!!errors.id}
                  readOnly={!inCreationMode} />
                <Form.Text className="text-muted">
                  You can't change the id after the creation.
                </Form.Text>
                <Form.Control.Feedback type="invalid">
                  {errors.id}
                </Form.Control.Feedback>
              </Form.Group>

              <Form.Group className="required" controlId="formName">
                <Form.Label>Name</Form.Label>
                <Form.Control type="text" name="name" placeholder="Name of the channel" required
                  value={values.name} onChange={handleChange} onBlur={handleBlur} isInvalid={!!errors.name} />
                <Form.Control.Feedback type="invalid">
                  {errors.name}
                </Form.Control.Feedback>
              </Form.Group>

              <Form.Group controlId="formDescription">
                <Form.Label>Description</Form.Label>
                <Form.Control as="textarea" name="description" placeholder="Description of the channel"
                  value={values.description} onChange={handleChange} onBlur={handleBlur} />
              </Form.Group>
            </Modal.Body>

            <Modal.Footer>
              <Button variant="secondary" onClick={this.handleCancel}>
                Cancel
              </Button>
              <Button variant="primary" type="submit" disabled={isSubmitting}>
                { saveLabel }
              </Button>
            </Modal.Footer>

          </Form>)}
        </Formik>
      </Modal>
    );
  }

}